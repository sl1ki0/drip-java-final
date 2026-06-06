package com.newsaggregator.repository;

import com.newsaggregator.db.Database;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;
import com.newsaggregator.model.Source;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteArticleRepositoryTest {
    private Database database;
    private SqliteArticleRepository repository;
    private int sourceId;

    @BeforeEach
    void setUp() {
        database = new Database("jdbc:sqlite::memory:");
        repository = new SqliteArticleRepository(database.getConnection());
        SqliteSourceRepository sources = new SqliteSourceRepository(database.getConnection());
        sourceId = sources.save(new Source("Test", "http://test/rss"));
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    void savesAndDetectsByUrl() {
        repository.save(sample("http://test/1"));
        assertTrue(repository.existsByUrl("http://test/1"));
        assertFalse(repository.existsByUrl("http://test/unknown"));
    }

    @Test
    void mapsFieldsBack() {
        int id = repository.save(sample("http://test/2"));
        Article loaded = repository.findById(id).orElseThrow();
        assertEquals("Заголовок", loaded.getTitle());
        assertEquals(Category.SCIENCE, loaded.getCategory());
        assertEquals(List.of("наука", "космос"), loaded.getKeywords());
    }

    @Test
    void deletesById() {
        int id = repository.save(sample("http://test/3"));
        assertTrue(repository.deleteById(id));
        assertEquals(0, repository.findAll().size());
    }

    private Article sample(String url) {
        Article a = new Article();
        a.setSourceId(sourceId);
        a.setUrl(url);
        a.setTitle("Заголовок");
        a.setSummary("Описание");
        a.setContent("Текст статьи");
        a.setCategory(Category.SCIENCE);
        a.setKeywords(new java.util.ArrayList<>(List.of("наука", "космос")));
        a.setPublishedAt(Instant.parse("2025-01-01T10:00:00Z"));
        a.setFetchedAt(Instant.now());
        return a;
    }
}
