package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsServiceTest {
    private final AnalyticsService analytics = new AnalyticsService();

    @Test
    void countsByCategory() {
        List<Article> articles = List.of(
                article("a", Category.SPORT, List.of("матч"), Instant.now()),
                article("b", Category.SPORT, List.of("гол"), Instant.now()),
                article("c", Category.ECONOMY, List.of("рубль"), Instant.now()));
        Map<Category, Long> counts = analytics.countByCategory(articles);
        assertEquals(2L, counts.get(Category.SPORT));
        assertEquals(1L, counts.get(Category.ECONOMY));
    }

    @Test
    void topKeywordsOrdersByFrequency() {
        List<Article> articles = List.of(
                article("a", Category.OTHER, List.of("выборы", "налоги"), Instant.now()),
                article("b", Category.OTHER, List.of("выборы"), Instant.now()));
        Map<String, Integer> top = analytics.topKeywords(articles, 5);
        assertEquals("выборы", top.keySet().iterator().next());
    }

    @Test
    void keywordDynamicsCountsByDay() {
        List<Article> articles = List.of(
                article("a", Category.OTHER, List.of("кризис"), Instant.parse("2025-01-01T10:00:00Z")),
                article("b", Category.OTHER, List.of("кризис"), Instant.parse("2025-01-01T12:00:00Z")));
        Map<java.time.LocalDate, Long> dynamics = analytics.keywordDynamics(articles, "кризис");
        assertTrue(dynamics.values().stream().mapToLong(Long::longValue).sum() >= 2);
    }

    private Article article(String url, Category category, List<String> keywords, Instant published) {
        Article a = new Article();
        a.setUrl(url);
        a.setTitle(url);
        a.setCategory(category);
        a.setKeywords(new java.util.ArrayList<>(keywords));
        a.setPublishedAt(published);
        return a;
    }
}
