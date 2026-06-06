package com.newsaggregator.support;

import com.newsaggregator.model.Article;
import com.newsaggregator.repository.ArticleRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeArticleRepository implements ArticleRepository {
    private final List<Article> storage = new ArrayList<>();
    private int sequence = 0;

    @Override
    public boolean existsByUrl(String url) {
        return storage.stream().anyMatch(a -> a.getUrl().equals(url));
    }

    @Override
    public int save(Article article) {
        article.setId(++sequence);
        storage.add(article);
        return article.getId();
    }

    @Override
    public List<Article> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public Optional<Article> findById(int id) {
        return storage.stream().filter(a -> a.getId() == id).findFirst();
    }

    @Override
    public boolean deleteById(int id) {
        return storage.removeIf(a -> a.getId() == id);
    }

    @Override
    public List<Article> findOlderThanDays(int days) {
        Instant threshold = Instant.now().minusSeconds((long) days * 86400);
        List<Article> result = new ArrayList<>();
        for (Article a : storage) {
            if (a.getPublishedAt() != null && a.getPublishedAt().isBefore(threshold)) {
                result.add(a);
            }
        }
        return result;
    }
}
