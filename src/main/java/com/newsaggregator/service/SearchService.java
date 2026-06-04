package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.repository.ArticleRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchService {
    private final ArticleRepository articleRepository;

    public SearchService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public List<Article> findAll(SortBy sortBy) {
        return sort(articleRepository.findAll(), sortBy);
    }

    public List<Article> search(SearchCriteria criteria, SortBy sortBy) {
        List<Article> result = articleRepository.findAll().stream()
                .filter(a -> matchesText(a, criteria.getText()))
                .filter(a -> matchesCategory(a, criteria))
                .filter(a -> matchesSource(a, criteria))
                .filter(a -> matchesKeyword(a, criteria.getKeyword()))
                .filter(a -> matchesFrom(a, criteria))
                .filter(a -> matchesTo(a, criteria))
                .collect(Collectors.toList());
        return sort(result, sortBy);
    }

    public List<Article> sort(List<Article> articles, SortBy sortBy) {
        Comparator<Article> comparator;
        switch (sortBy) {
            case DATE_ASC:
                comparator = Comparator.comparing(Article::getPublishedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case SOURCE:
                comparator = Comparator.comparing(a -> a.getSourceName() == null ? "" : a.getSourceName());
                break;
            case POPULARITY:
                Map<Integer, Integer> scores = popularityScores(articles);
                comparator = Comparator.comparingInt((Article a) -> scores.getOrDefault(a.getId(), 0)).reversed();
                break;
            case DATE_DESC:
            default:
                comparator = Comparator.comparing(Article::getPublishedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        }
        return articles.stream().sorted(comparator).collect(Collectors.toList());
    }

    private Map<Integer, Integer> popularityScores(List<Article> articles) {
        Map<String, Integer> keywordFrequency = new HashMap<>();
        for (Article a : articles) {
            for (String keyword : a.getKeywords()) {
                keywordFrequency.merge(keyword, 1, Integer::sum);
            }
        }
        Map<Integer, Integer> scores = new HashMap<>();
        for (Article a : articles) {
            int score = 0;
            for (String keyword : a.getKeywords()) {
                score += keywordFrequency.getOrDefault(keyword, 0);
            }
            scores.put(a.getId(), score);
        }
        return scores;
    }

    private boolean matchesText(Article a, String text) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String needle = text.toLowerCase();
        return contains(a.getTitle(), needle) || contains(a.getContent(), needle)
                || contains(a.getSummary(), needle);
    }

    private boolean matchesCategory(Article a, SearchCriteria criteria) {
        return criteria.getCategory() == null || criteria.getCategory() == a.getCategory();
    }

    private boolean matchesSource(Article a, SearchCriteria criteria) {
        if (criteria.getSourceName() == null || criteria.getSourceName().isBlank()) {
            return true;
        }
        return a.getSourceName() != null
                && a.getSourceName().equalsIgnoreCase(criteria.getSourceName());
    }

    private boolean matchesKeyword(Article a, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String needle = keyword.toLowerCase();
        return a.getKeywords().stream().anyMatch(k -> k.toLowerCase().contains(needle));
    }

    private boolean matchesFrom(Article a, SearchCriteria criteria) {
        if (criteria.getFrom() == null) {
            return true;
        }
        return a.getPublishedAt() != null && !a.getPublishedAt().isBefore(criteria.getFrom());
    }

    private boolean matchesTo(Article a, SearchCriteria criteria) {
        if (criteria.getTo() == null) {
            return true;
        }
        return a.getPublishedAt() != null && !a.getPublishedAt().isAfter(criteria.getTo());
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }
}
