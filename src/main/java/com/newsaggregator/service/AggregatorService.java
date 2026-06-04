package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.ChangeLogEntry;
import com.newsaggregator.model.Source;
import com.newsaggregator.parser.ContentEnricher;
import com.newsaggregator.parser.EnrichedContent;
import com.newsaggregator.parser.NewsParser;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.repository.ChangeLogRepository;
import com.newsaggregator.repository.SourceRepository;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AggregatorService {
    private final SourceRepository sourceRepository;
    private final ArticleRepository articleRepository;
    private final ChangeLogRepository changeLogRepository;
    private final NewsParser newsParser;
    private final ContentEnricher contentEnricher;
    private final CategoryClassifier categoryClassifier;
    private final KeywordExtractor keywordExtractor;
    private final boolean fetchFullText;

    public AggregatorService(SourceRepository sourceRepository,
                             ArticleRepository articleRepository,
                             ChangeLogRepository changeLogRepository,
                             NewsParser newsParser,
                             ContentEnricher contentEnricher,
                             CategoryClassifier categoryClassifier,
                             KeywordExtractor keywordExtractor,
                             boolean fetchFullText) {
        this.sourceRepository = sourceRepository;
        this.articleRepository = articleRepository;
        this.changeLogRepository = changeLogRepository;
        this.newsParser = newsParser;
        this.contentEnricher = contentEnricher;
        this.categoryClassifier = categoryClassifier;
        this.keywordExtractor = keywordExtractor;
        this.fetchFullText = fetchFullText;
    }

    public CollectResult collect() {
        CollectResult result = new CollectResult();
        for (Source source : sourceRepository.findEnabled()) {
            collectFromSource(source, result);
        }
        return result;
    }

    private void collectFromSource(Source source, CollectResult result) {
        List<Article> articles;
        try {
            articles = newsParser.fetchArticles(source);
        } catch (RuntimeException e) {
            result.addError(source.getName() + ": " + e.getMessage());
            return;
        }
        for (Article article : articles) {
            if (article.getUrl() == null || articleRepository.existsByUrl(article.getUrl())) {
                result.incrementSkipped();
                continue;
            }
            enrich(article);
            articleRepository.save(article);
            changeLogRepository.add(new ChangeLogEntry(
                    "ADD", article.getId(), Instant.now(), article.getTitle()));
            result.incrementAdded();
        }
    }

    private void enrich(Article article) {
        if (fetchFullText) {
            EnrichedContent enriched = contentEnricher.enrich(article.getUrl());
            if (enriched.text() != null && !enriched.text().isBlank()) {
                article.setContent(enriched.text());
            }
            Set<String> media = new LinkedHashSet<>(article.getMediaLinks());
            media.addAll(enriched.media());
            article.setMediaLinks(new java.util.ArrayList<>(media));
        }
        if (article.getContent() == null || article.getContent().isBlank()) {
            article.setContent(article.getSummary());
        }

        String basis = (article.getTitle() == null ? "" : article.getTitle()) + " "
                + (article.getSummary() == null ? "" : article.getSummary()) + " "
                + (article.getContent() == null ? "" : article.getContent());
        article.setCategory(categoryClassifier.classify(basis));
        article.setKeywords(keywordExtractor.extractKeywords(basis, 8));
        if (article.getSummary() == null || article.getSummary().isBlank()) {
            article.setSummary(keywordExtractor.summarize(article.getContent(), 2));
        }
        article.setFetchedAt(Instant.now());
    }

    public int removeOlderThan(int days) {
        int removed = 0;
        for (Article article : articleRepository.findOlderThanDays(days)) {
            if (articleRepository.deleteById(article.getId())) {
                changeLogRepository.add(new ChangeLogEntry(
                        "REMOVE", article.getId(), Instant.now(), article.getTitle()));
                removed++;
            }
        }
        return removed;
    }

    public List<ChangeLogEntry> recentChanges(int limit) {
        return changeLogRepository.findRecent(limit);
    }
}
