package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Source;
import com.newsaggregator.support.FakeArticleRepository;
import com.newsaggregator.support.FakeChangeLogRepository;
import com.newsaggregator.support.FakeContentEnricher;
import com.newsaggregator.support.FakeNewsParser;
import com.newsaggregator.support.FakeSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregatorServiceTest {
    private FakeArticleRepository articleRepository;
    private FakeChangeLogRepository changeLogRepository;
    private AggregatorService aggregator;

    @BeforeEach
    void setUp() {
        FakeSourceRepository sourceRepository = new FakeSourceRepository();
        sourceRepository.save(new Source("Test", "http://test/rss"));
        articleRepository = new FakeArticleRepository();
        changeLogRepository = new FakeChangeLogRepository();
        FakeNewsParser parser = new FakeNewsParser(List.of(
                article("http://test/1", "Сборная выиграла матч"),
                article("http://test/2", "Курс рубля вырос")));
        aggregator = new AggregatorService(sourceRepository, articleRepository, changeLogRepository,
                parser, new FakeContentEnricher(), new CategoryClassifier(), new KeywordExtractor(), false);
    }

    @Test
    void collectsNewArticles() {
        CollectResult result = aggregator.collect();
        assertEquals(2, result.getAdded());
        assertEquals(2, articleRepository.findAll().size());
        assertEquals(2, changeLogRepository.all().size());
    }

    @Test
    void doesNotDuplicateOnSecondRun() {
        aggregator.collect();
        CollectResult second = aggregator.collect();
        assertEquals(0, second.getAdded());
        assertEquals(2, second.getSkipped());
        assertEquals(2, articleRepository.findAll().size());
    }

    @Test
    void assignsCategoryAndKeywords() {
        aggregator.collect();
        Article first = articleRepository.findAll().get(0);
        org.junit.jupiter.api.Assertions.assertNotNull(first.getCategory());
        org.junit.jupiter.api.Assertions.assertFalse(first.getKeywords().isEmpty());
    }

    private Article article(String url, String title) {
        Article a = new Article();
        a.setUrl(url);
        a.setTitle(title);
        a.setSummary(title);
        return a;
    }
}
