package com.newsaggregator.repository;

import com.newsaggregator.model.ChangeLogEntry;

import java.util.List;

public interface ChangeLogRepository {
    void add(ChangeLogEntry entry);

    List<ChangeLogEntry> findRecent(int limit);
}
