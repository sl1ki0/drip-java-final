package com.newsaggregator.service;

import com.newsaggregator.model.Category;

import java.time.Instant;

public class SearchCriteria {
    private String text;
    private Category category;
    private String sourceName;
    private String keyword;
    private Instant from;
    private Instant to;

    public String getText() {
        return text;
    }

    public SearchCriteria text(String text) {
        this.text = text;
        return this;
    }

    public Category getCategory() {
        return category;
    }

    public SearchCriteria category(Category category) {
        this.category = category;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public SearchCriteria sourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public SearchCriteria keyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public Instant getFrom() {
        return from;
    }

    public SearchCriteria from(Instant from) {
        this.from = from;
        return this;
    }

    public Instant getTo() {
        return to;
    }

    public SearchCriteria to(Instant to) {
        this.to = to;
        return this;
    }
}
