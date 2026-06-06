package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.support.FakeArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {
    private FakeArticleRepository repository;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        repository = new FakeArticleRepository();
        searchService = new SearchService(repository);
        repository.save(article("Спорт сегодня", "sport", Category.SPORT,
                Instant.parse("2025-01-01T10:00:00Z"), List.of("матч")));
        repository.save(article("Экономика растёт", "economy", Category.ECONOMY,
                Instant.parse("2025-02-01T10:00:00Z"), List.of("рубль")));
    }

    @Test
    void filtersByCategory() {
        List<Article> result = searchService.search(
                new SearchCriteria().category(Category.SPORT), SortBy.DATE_DESC);
        assertEquals(1, result.size());
        assertEquals("Спорт сегодня", result.get(0).getTitle());
    }

    @Test
    void searchesByText() {
        List<Article> result = searchService.search(
                new SearchCriteria().text("растёт"), SortBy.DATE_DESC);
        assertEquals(1, result.size());
        assertEquals(Category.ECONOMY, result.get(0).getCategory());
    }

    @Test
    void filtersByKeyword() {
        List<Article> result = searchService.search(
                new SearchCriteria().keyword("матч"), SortBy.DATE_DESC);
        assertEquals(1, result.size());
    }

    @Test
    void sortsByDateDescending() {
        List<Article> result = searchService.search(new SearchCriteria(), SortBy.DATE_DESC);
        assertTrue(result.get(0).getPublishedAt().isAfter(result.get(1).getPublishedAt()));
    }

    private Article article(String title, String url, Category category,
                            Instant published, List<String> keywords) {
        Article a = new Article();
        a.setTitle(title);
        a.setUrl(url);
        a.setCategory(category);
        a.setPublishedAt(published);
        a.setKeywords(new java.util.ArrayList<>(keywords));
        return a;
    }
}
