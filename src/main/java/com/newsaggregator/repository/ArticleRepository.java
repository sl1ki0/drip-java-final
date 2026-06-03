package com.newsaggregator.repository;

import com.newsaggregator.model.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    boolean existsByUrl(String url);

    int save(Article article);

    List<Article> findAll();

    Optional<Article> findById(int id);

    boolean deleteById(int id);

    List<Article> findOlderThanDays(int days);
}
