package com.newsaggregator.service;

import java.util.ArrayList;
import java.util.List;

public class CollectResult {
    private int added;
    private int skipped;
    private final List<String> errors = new ArrayList<>();

    public void incrementAdded() {
        added++;
    }

    public void incrementSkipped() {
        skipped++;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public int getAdded() {
        return added;
    }

    public int getSkipped() {
        return skipped;
    }

    public List<String> getErrors() {
        return errors;
    }
}
