package com.newsaggregator.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeywordExtractorTest {
    private final KeywordExtractor extractor = new KeywordExtractor();

    @Test
    void picksMostFrequentWord() {
        String text = "Экономика экономика экономика рост рост развитие";
        List<String> keywords = extractor.extractKeywords(text, 3);
        assertEquals("экономика", keywords.get(0));
    }

    @Test
    void skipsShortAndStopWords() {
        List<String> keywords = extractor.extractKeywords("и в на это от его", 5);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void summarizeReturnsFirstSentences() {
        String text = "Первое предложение. Второе предложение. Третье предложение.";
        String summary = extractor.summarize(text, 2);
        assertEquals("Первое предложение. Второе предложение.", summary);
    }

    @Test
    void summarizeEmptyText() {
        assertEquals("", extractor.summarize("", 2));
    }
}
