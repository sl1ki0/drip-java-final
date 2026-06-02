package com.newsaggregator.model;

public class Source {
    private int id;
    private String name;
    private String rssUrl;
    private boolean enabled;

    public Source(int id, String name, String rssUrl, boolean enabled) {
        this.id = id;
        this.name = name;
        this.rssUrl = rssUrl;
        this.enabled = enabled;
    }

    public Source(String name, String rssUrl) {
        this(0, name, rssUrl, true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
