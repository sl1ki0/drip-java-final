package com.newsaggregator.parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

final class JsoupSupport {
    private JsoupSupport() {
    }

    static Connection connect(String url, int timeoutMs) {
        return Jsoup.connect(url)
                .timeout(timeoutMs)
                .userAgent("Mozilla/5.0 NewsAggregator");
    }
}
