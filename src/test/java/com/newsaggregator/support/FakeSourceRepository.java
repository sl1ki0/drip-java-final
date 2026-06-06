package com.newsaggregator.support;

import com.newsaggregator.model.Source;
import com.newsaggregator.repository.SourceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FakeSourceRepository implements SourceRepository {
    private final List<Source> storage = new ArrayList<>();
    private int sequence = 0;

    @Override
    public int save(Source source) {
        source.setId(++sequence);
        storage.add(source);
        return source.getId();
    }

    @Override
    public List<Source> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public List<Source> findEnabled() {
        return storage.stream().filter(Source::isEnabled).collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(int id) {
        return storage.removeIf(s -> s.getId() == id);
    }

    @Override
    public boolean setEnabled(int id, boolean enabled) {
        for (Source s : storage) {
            if (s.getId() == id) {
                s.setEnabled(enabled);
                return true;
            }
        }
        return false;
    }
}
