package com.newsaggregator.support;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Source;
import com.newsaggregator.parser.NewsParser;

import java.util.ArrayList;
import java.util.List;

public class FakeNewsParser implements NewsParser {
    private final List<Article> articles;

    public FakeNewsParser(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public List<Article> fetchArticles(Source source) {
        return new ArrayList<>(articles);
    }
}
