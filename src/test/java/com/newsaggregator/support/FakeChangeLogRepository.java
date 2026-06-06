package com.newsaggregator.support;

import com.newsaggregator.model.ChangeLogEntry;
import com.newsaggregator.repository.ChangeLogRepository;

import java.util.ArrayList;
import java.util.List;

public class FakeChangeLogRepository implements ChangeLogRepository {
    private final List<ChangeLogEntry> storage = new ArrayList<>();

    @Override
    public void add(ChangeLogEntry entry) {
        storage.add(entry);
    }

    @Override
    public List<ChangeLogEntry> findRecent(int limit) {
        return new ArrayList<>(storage);
    }

    public List<ChangeLogEntry> all() {
        return storage;
    }
}
