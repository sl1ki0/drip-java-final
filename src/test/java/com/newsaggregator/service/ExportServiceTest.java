package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportServiceTest {
    private final ExportService exportService = new ExportService();

    @Test
    void csvContainsHeaderAndTitle() {
        String csv = exportService.export(List.of(sample()), ExportFormat.CSV);
        assertTrue(csv.startsWith("id,source,url,title"));
        assertTrue(csv.contains("Заголовок"));
    }

    @Test
    void jsonContainsTitle() {
        String json = exportService.export(List.of(sample()), ExportFormat.JSON);
        assertTrue(json.contains("Заголовок"));
    }

    @Test
    void htmlContainsTable() {
        String html = exportService.export(List.of(sample()), ExportFormat.HTML);
        assertTrue(html.contains("<table"));
        assertTrue(html.contains("Заголовок"));
    }

    private Article sample() {
        Article a = new Article();
        a.setId(1);
        a.setTitle("Заголовок");
        a.setUrl("http://test/1");
        a.setSourceName("Источник");
        a.setCategory(Category.SCIENCE);
        a.setPublishedAt(Instant.parse("2025-01-01T10:00:00Z"));
        a.setSummary("Краткое описание");
        return a;
    }
}
