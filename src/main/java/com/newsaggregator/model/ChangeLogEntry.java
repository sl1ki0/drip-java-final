package com.newsaggregator.model;

import java.time.Instant;

public class ChangeLogEntry {
    private int id;
    private String action;
    private Integer articleId;
    private Instant timestamp;
    private String details;

    public ChangeLogEntry(String action, Integer articleId, Instant timestamp, String details) {
        this.action = action;
        this.articleId = articleId;
        this.timestamp = timestamp;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }
}
