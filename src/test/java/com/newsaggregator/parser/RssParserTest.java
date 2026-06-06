package com.newsaggregator.parser;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Source;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RssParserTest {
    private final RssParser parser = new RssParser();

    @Test
    void parsesItems() throws IOException {
        List<Article> articles = parser.parse(loadFixture(), new Source("Test", "http://test/rss"));
        assertEquals(2, articles.size());
        assertEquals("Первая новость", articles.get(0).getTitle());
        assertEquals("http://test/news/1", articles.get(0).getUrl());
        assertNotNull(articles.get(0).getPublishedAt());
    }

    @Test
    void extractsEnclosureMedia() throws IOException {
        List<Article> articles = parser.parse(loadFixture(), new Source("Test", "http://test/rss"));
        assertTrue(articles.get(0).getMediaLinks().contains("http://test/img/1.jpg"));
    }

    @Test
    void stripsHtmlFromSummary() throws IOException {
        List<Article> articles = parser.parse(loadFixture(), new Source("Test", "http://test/rss"));
        assertEquals("Краткое описание первой новости", articles.get(0).getSummary());
    }

    private String loadFixture() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sample_rss.xml")) {
            assertNotNull(in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
