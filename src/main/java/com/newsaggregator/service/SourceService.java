package com.newsaggregator.service;

import com.newsaggregator.model.Source;
import com.newsaggregator.repository.SourceRepository;

import java.util.List;

public class SourceService {
    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    public boolean isEmpty() {
        return sourceRepository.findAll().isEmpty();
    }

    public List<Source> list() {
        return sourceRepository.findAll();
    }

    public boolean add(String name, String rssUrl) {
        return sourceRepository.save(new Source(name, rssUrl)) > 0;
    }

    public boolean delete(int id) {
        return sourceRepository.deleteById(id);
    }

    public boolean setEnabled(int id, boolean enabled) {
        return sourceRepository.setEnabled(id, enabled);
    }

    public int importDefaults(List<Source> sources) {
        int added = 0;
        for (Source source : sources) {
            if (sourceRepository.save(source) > 0) {
                added++;
            }
        }
        return added;
    }
}
