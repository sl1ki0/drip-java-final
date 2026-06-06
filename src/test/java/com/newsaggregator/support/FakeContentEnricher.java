package com.newsaggregator.support;

import com.newsaggregator.parser.ContentEnricher;
import com.newsaggregator.parser.EnrichedContent;

public class FakeContentEnricher implements ContentEnricher {
    @Override
    public EnrichedContent enrich(String url) {
        return EnrichedContent.empty();
    }
}
