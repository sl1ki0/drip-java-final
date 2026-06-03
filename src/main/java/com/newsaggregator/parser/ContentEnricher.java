package com.newsaggregator.parser;

public interface ContentEnricher {
    EnrichedContent enrich(String url);
}
