package com.newsaggregator;

import com.google.gson.Gson;
import com.newsaggregator.db.Database;
import com.newsaggregator.model.Source;
import com.newsaggregator.parser.ArticleScraper;
import com.newsaggregator.parser.RssParser;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.repository.ChangeLogRepository;
import com.newsaggregator.repository.SourceRepository;
import com.newsaggregator.repository.SqliteArticleRepository;
import com.newsaggregator.repository.SqliteChangeLogRepository;
import com.newsaggregator.repository.SqliteSourceRepository;
import com.newsaggregator.service.AggregatorService;
import com.newsaggregator.service.AnalyticsService;
import com.newsaggregator.service.CategoryClassifier;
import com.newsaggregator.service.ExportService;
import com.newsaggregator.service.KeywordExtractor;
import com.newsaggregator.service.RefreshScheduler;
import com.newsaggregator.service.SearchService;
import com.newsaggregator.service.SourceService;
import com.newsaggregator.ui.ConsoleApp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:news.db";
        try (Database database = new Database(dbUrl)) {
            ArticleRepository articleRepository = new SqliteArticleRepository(database.getConnection());
            SourceRepository sourceRepository = new SqliteSourceRepository(database.getConnection());
            ChangeLogRepository changeLogRepository = new SqliteChangeLogRepository(database.getConnection());

            RssParser rssParser = new RssParser();
            ArticleScraper scraper = new ArticleScraper();
            CategoryClassifier classifier = new CategoryClassifier();
            KeywordExtractor keywordExtractor = new KeywordExtractor();

            AggregatorService aggregatorService = new AggregatorService(
                    sourceRepository, articleRepository, changeLogRepository,
                    rssParser, scraper, classifier, keywordExtractor, true);
            SearchService searchService = new SearchService(articleRepository);
            AnalyticsService analyticsService = new AnalyticsService();
            ExportService exportService = new ExportService();
            SourceService sourceService = new SourceService(sourceRepository);
            RefreshScheduler scheduler = new RefreshScheduler();

            ConsoleApp consoleApp = new ConsoleApp(
                    aggregatorService, searchService, analyticsService, exportService,
                    sourceService, scheduler, loadDefaultSources());
            consoleApp.run();
        }
    }

    private static List<Source> loadDefaultSources() {
        try (InputStream in = App.class.getClassLoader().getResourceAsStream("sources.json")) {
            if (in == null) {
                return new ArrayList<>();
            }
            Gson gson = new Gson();
            SourceConfig[] configs = gson.fromJson(
                    new InputStreamReader(in, StandardCharsets.UTF_8), SourceConfig[].class);
            List<Source> sources = new ArrayList<>();
            for (SourceConfig config : configs) {
                sources.add(new Source(config.name, config.rssUrl));
            }
            return sources;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static class SourceConfig {
        String name;
        String rssUrl;
    }
}
