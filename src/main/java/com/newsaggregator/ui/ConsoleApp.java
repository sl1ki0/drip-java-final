package com.newsaggregator.ui;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.ChangeLogEntry;
import com.newsaggregator.model.Source;
import com.newsaggregator.service.AggregatorService;
import com.newsaggregator.service.AnalyticsService;
import com.newsaggregator.service.CollectResult;
import com.newsaggregator.service.ExportFormat;
import com.newsaggregator.service.ExportService;
import com.newsaggregator.service.SearchCriteria;
import com.newsaggregator.service.SearchService;
import com.newsaggregator.service.SourceService;
import com.newsaggregator.service.RefreshScheduler;
import com.newsaggregator.service.SortBy;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleApp {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AggregatorService aggregatorService;
    private final SearchService searchService;
    private final AnalyticsService analyticsService;
    private final ExportService exportService;
    private final SourceService sourceService;
    private final RefreshScheduler scheduler;
    private final List<Source> defaultSources;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApp(AggregatorService aggregatorService,
                      SearchService searchService,
                      AnalyticsService analyticsService,
                      ExportService exportService,
                      SourceService sourceService,
                      RefreshScheduler scheduler,
                      List<Source> defaultSources) {
        this.aggregatorService = aggregatorService;
        this.searchService = searchService;
        this.analyticsService = analyticsService;
        this.exportService = exportService;
        this.sourceService = sourceService;
        this.scheduler = scheduler;
        this.defaultSources = defaultSources;
    }

    public void run() {
        if (sourceService.isEmpty()) {
            int added = sourceService.importDefaults(defaultSources);
            System.out.println("Загружены источники по умолчанию: " + added);
        }
        System.out.println("=== Агрегатор новостей ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Выбор: ");
            switch (choice) {
                case "1" -> collectNews();
                case "2" -> showArticles();
                case "3" -> searchMenu();
                case "4" -> analyticsMenu();
                case "5" -> sourcesMenu();
                case "6" -> exportMenu();
                case "7" -> schedulerMenu();
                case "8" -> showHistory();
                case "9" -> removeOld();
                case "0" -> running = false;
                default -> System.out.println("Неизвестная команда");
            }
        }
        scheduler.stop();
        System.out.println("Выход.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Обновить новости (собрать)");
        System.out.println("2. Показать новости");
        System.out.println("3. Поиск и фильтрация");
        System.out.println("4. Аналитика");
        System.out.println("5. Источники");
        System.out.println("6. Экспорт");
        System.out.println("7. Автообновление");
        System.out.println("8. История изменений");
        System.out.println("9. Удалить устаревшие новости");
        System.out.println("0. Выход");
    }

    private void collectNews() {
        System.out.println("Сбор новостей...");
        CollectResult result = aggregatorService.collect();
        System.out.println("Добавлено: " + result.getAdded() + ", пропущено (дубли): " + result.getSkipped());
        for (String error : result.getErrors()) {
            System.out.println("Ошибка источника: " + error);
        }
    }

    private void showArticles() {
        SortBy sortBy = askSort();
        printArticles(searchService.findAll(sortBy));
    }

    private void searchMenu() {
        SearchCriteria criteria = new SearchCriteria();
        String text = prompt("Текст в заголовке/тексте (Enter — пропустить): ");
        if (!text.isBlank()) {
            criteria.text(text);
        }
        Category category = askCategoryOptional();
        if (category != null) {
            criteria.category(category);
        }
        String source = prompt("Источник (Enter — пропустить): ");
        if (!source.isBlank()) {
            criteria.sourceName(source);
        }
        String keyword = prompt("Ключевое слово (Enter — пропустить): ");
        if (!keyword.isBlank()) {
            criteria.keyword(keyword);
        }
        String daysText = prompt("За последние N дней (Enter — пропустить): ");
        if (!daysText.isBlank()) {
            int days = parseInt(daysText, 0);
            if (days > 0) {
                criteria.from(Instant.now().minus(Duration.ofDays(days)));
            }
        }
        SortBy sortBy = askSort();
        printArticles(searchService.search(criteria, sortBy));
    }

    private void analyticsMenu() {
        System.out.println("1. Количество по категориям");
        System.out.println("2. Топ ключевых слов");
        System.out.println("3. Топ упоминаемых персон");
        System.out.println("4. Динамика по ключевому слову");
        System.out.println("5. Трендовые темы за период");
        String choice = prompt("Выбор: ");
        List<Article> all = searchService.findAll();
        switch (choice) {
            case "1" -> {
                Map<Category, Long> counts = analyticsService.countByCategory(all);
                counts.forEach((c, n) -> System.out.println(c.getTitle() + ": " + n));
            }
            case "2" -> printFrequency(analyticsService.topKeywords(all, 10));
            case "3" -> printFrequency(analyticsService.topPersons(all, 10));
            case "4" -> {
                String keyword = prompt("Ключевое слово: ");
                Map<LocalDate, Long> dynamics = analyticsService.keywordDynamics(all, keyword);
                if (dynamics.isEmpty()) {
                    System.out.println("Нет упоминаний.");
                } else {
                    dynamics.forEach((day, n) -> System.out.println(day + ": " + n));
                }
            }
            case "5" -> {
                int days = parseInt(prompt("За последние N дней: "), 7);
                Instant from = Instant.now().minus(Duration.ofDays(days));
                printFrequency(analyticsService.trendingTopics(all, from, Instant.now(), 10));
            }
            default -> System.out.println("Неизвестная команда");
        }
    }

    private void sourcesMenu() {
        System.out.println("1. Список");
        System.out.println("2. Добавить");
        System.out.println("3. Удалить по id");
        System.out.println("4. Вкл/выкл по id");
        System.out.println("5. Загрузить источники по умолчанию");
        String choice = prompt("Выбор: ");
        switch (choice) {
            case "1" -> {
                for (Source s : sourceService.list()) {
                    System.out.printf("[%d] %s | %s | %s%n", s.getId(), s.getName(),
                            s.getRssUrl(), s.isEnabled() ? "вкл" : "выкл");
                }
            }
            case "2" -> {
                String name = prompt("Название: ");
                String url = prompt("RSS URL: ");
                if (!name.isBlank() && !url.isBlank()) {
                    sourceService.add(name, url);
                    System.out.println("Добавлено.");
                }
            }
            case "3" -> {
                int id = parseInt(prompt("id: "), -1);
                System.out.println(sourceService.delete(id) ? "Удалено." : "Не найдено.");
            }
            case "4" -> {
                int id = parseInt(prompt("id: "), -1);
                String state = prompt("Включить? (y/n): ");
                boolean enabled = state.equalsIgnoreCase("y");
                System.out.println(sourceService.setEnabled(id, enabled) ? "Готово." : "Не найдено.");
            }
            case "5" -> {
                int added = sourceService.importDefaults(defaultSources);
                System.out.println("Добавлено источников: " + added);
            }
            default -> System.out.println("Неизвестная команда");
        }
    }

    private void exportMenu() {
        ExportFormat format = askFormat();
        if (format == null) {
            return;
        }
        String fileName = prompt("Имя файла (Enter — авто): ");
        if (fileName.isBlank()) {
            fileName = "export." + format.name().toLowerCase();
        }
        Path path = Path.of("exports", fileName);
        try {
            java.nio.file.Files.createDirectories(path.getParent());
        } catch (Exception e) {
            System.out.println("Не удалось создать каталог exports.");
            return;
        }
        exportService.exportToFile(searchService.findAll(), format, path);
        System.out.println("Сохранено: " + path.toAbsolutePath());
    }

    private void schedulerMenu() {
        if (scheduler.isRunning()) {
            scheduler.stop();
            System.out.println("Автообновление выключено.");
            return;
        }
        int seconds = parseInt(prompt("Интервал, сек: "), 0);
        if (seconds <= 0) {
            System.out.println("Некорректный интервал.");
            return;
        }
        scheduler.start(() -> {
            CollectResult result = aggregatorService.collect();
            System.out.println("[авто] добавлено: " + result.getAdded());
        }, seconds);
        System.out.println("Автообновление включено, интервал " + seconds + " сек.");
    }

    private void showHistory() {
        List<ChangeLogEntry> entries = aggregatorService.recentChanges(20);
        if (entries.isEmpty()) {
            System.out.println("История пуста.");
            return;
        }
        for (ChangeLogEntry e : entries) {
            System.out.printf("%s | %s | %s%n",
                    DATE_FORMAT.format(e.getTimestamp()), e.getAction(),
                    e.getDetails() == null ? "" : e.getDetails());
        }
    }

    private void removeOld() {
        int days = parseInt(prompt("Удалить новости старше N дней: "), 0);
        if (days <= 0) {
            System.out.println("Некорректное значение.");
            return;
        }
        int removed = aggregatorService.removeOlderThan(days);
        System.out.println("Удалено: " + removed);
    }

    private void printArticles(List<Article> articles) {
        if (articles.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }
        int index = 1;
        for (Article a : articles) {
            System.out.printf("%d. %s%n", index++, a.getTitle());
            System.out.printf("   %s | %s | %s%n",
                    a.getSourceName() == null ? "?" : a.getSourceName(),
                    a.getPublishedAt() == null ? "—" : DATE_FORMAT.format(a.getPublishedAt()),
                    a.getCategory() == null ? "" : a.getCategory().getTitle());
            if (a.getSummary() != null && !a.getSummary().isBlank()) {
                System.out.println("   " + shorten(a.getSummary(), 160));
            }
            System.out.println("   " + a.getUrl());
        }
        System.out.println("Всего: " + articles.size());
    }

    private void printFrequency(Map<String, Integer> frequency) {
        if (frequency.isEmpty()) {
            System.out.println("Нет данных.");
            return;
        }
        frequency.forEach((key, count) -> System.out.println(key + ": " + count));
    }

    private SortBy askSort() {
        System.out.println("Сортировка: 1) дата (новые) 2) дата (старые) 3) источник 4) популярность");
        return switch (prompt("Выбор: ")) {
            case "2" -> SortBy.DATE_ASC;
            case "3" -> SortBy.SOURCE;
            case "4" -> SortBy.POPULARITY;
            default -> SortBy.DATE_DESC;
        };
    }

    private Category askCategoryOptional() {
        System.out.println("Категория (Enter — любая):");
        Category[] values = Category.values();
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, values[i].getTitle());
        }
        String input = prompt("Номер: ");
        if (input.isBlank()) {
            return null;
        }
        int num = parseInt(input, -1);
        if (num >= 1 && num <= values.length) {
            return values[num - 1];
        }
        return null;
    }

    private ExportFormat askFormat() {
        System.out.println("Формат: 1) CSV 2) JSON 3) HTML");
        return switch (prompt("Выбор: ")) {
            case "1" -> ExportFormat.CSV;
            case "2" -> ExportFormat.JSON;
            case "3" -> ExportFormat.HTML;
            default -> null;
        };
    }

    private String shorten(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...";
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        if (!scanner.hasNextLine()) {
            return "0";
        }
        return scanner.nextLine().trim();
    }
}
