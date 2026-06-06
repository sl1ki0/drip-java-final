package com.newsaggregator.service;

import com.newsaggregator.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryClassifierTest {
    private final CategoryClassifier classifier = new CategoryClassifier();

    @Test
    void detectsSport() {
        Category result = classifier.classify("Сборная выиграла матч и завоевала медаль на чемпионате");
        assertEquals(Category.SPORT, result);
    }

    @Test
    void detectsEconomy() {
        Category result = classifier.classify("Курс рубля и цены на нефть выросли на бирже");
        assertEquals(Category.ECONOMY, result);
    }

    @Test
    void returnsOtherForUnknownText() {
        assertEquals(Category.OTHER, classifier.classify("просто какой-то набор слов"));
    }

    @Test
    void returnsOtherForEmptyText() {
        assertEquals(Category.OTHER, classifier.classify(""));
    }
}
