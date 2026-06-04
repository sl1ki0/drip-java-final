package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyticsService {
    private static final Pattern PERSON_PATTERN =
            Pattern.compile("\\p{Lu}\\p{Ll}+\\s+\\p{Lu}\\p{Ll}+");

    public Map<Category, Long> countByCategory(List<Article> articles) {
        Map<Category, Long> counts = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            counts.put(category, 0L);
        }
        for (Article a : articles) {
            Category category = a.getCategory() == null ? Category.OTHER : a.getCategory();
            counts.merge(category, 1L, Long::sum);
        }
        return counts;
    }

    public Map<String, Integer> topKeywords(List<Article> articles, int limit) {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        for (Article a : articles) {
            for (String keyword : a.getKeywords()) {
                frequency.merge(keyword.toLowerCase(), 1, Integer::sum);
            }
        }
        return topEntries(frequency, limit);
    }

    public Map<String, Integer> topPersons(List<Article> articles, int limit) {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        for (Article a : articles) {
            String text = (a.getTitle() == null ? "" : a.getTitle()) + " "
                    + (a.getSummary() == null ? "" : a.getSummary());
            Matcher matcher = PERSON_PATTERN.matcher(text);
            while (matcher.find()) {
                frequency.merge(matcher.group(), 1, Integer::sum);
            }
        }
        return topEntries(frequency, limit);
    }

    public Map<LocalDate, Long> keywordDynamics(List<Article> articles, String keyword) {
        String needle = keyword.toLowerCase();
        Map<LocalDate, Long> byDay = new TreeMap<>();
        for (Article a : articles) {
            if (a.getPublishedAt() == null) {
                continue;
            }
            if (mentions(a, needle)) {
                LocalDate day = LocalDate.ofInstant(a.getPublishedAt(), ZoneId.systemDefault());
                byDay.merge(day, 1L, Long::sum);
            }
        }
        return byDay;
    }

    public Map<String, Integer> trendingTopics(List<Article> articles, Instant from, Instant to, int limit) {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        for (Article a : articles) {
            if (a.getPublishedAt() == null) {
                continue;
            }
            if (a.getPublishedAt().isBefore(from) || a.getPublishedAt().isAfter(to)) {
                continue;
            }
            for (String keyword : a.getKeywords()) {
                frequency.merge(keyword.toLowerCase(), 1, Integer::sum);
            }
        }
        return topEntries(frequency, limit);
    }

    private boolean mentions(Article a, String needle) {
        if (a.getKeywords().stream().anyMatch(k -> k.toLowerCase().contains(needle))) {
            return true;
        }
        if (a.getTitle() != null && a.getTitle().toLowerCase().contains(needle)) {
            return true;
        }
        return a.getContent() != null && a.getContent().toLowerCase().contains(needle);
    }

    private Map<String, Integer> topEntries(Map<String, Integer> frequency, int limit) {
        return Frequencies.top(frequency, limit);
    }
}
