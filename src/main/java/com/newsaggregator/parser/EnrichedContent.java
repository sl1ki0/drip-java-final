package com.newsaggregator.parser;

import java.util.List;

public record EnrichedContent(String text, List<String> media) {
    public static EnrichedContent empty() {
        return new EnrichedContent("", List.of());
    }
}
