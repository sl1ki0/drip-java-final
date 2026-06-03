package com.newsaggregator.parser;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Source;

import java.util.List;

public interface NewsParser {
    List<Article> fetchArticles(Source source);
}
