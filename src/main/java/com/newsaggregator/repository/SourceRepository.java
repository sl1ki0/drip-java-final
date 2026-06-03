package com.newsaggregator.repository;

import com.newsaggregator.model.Source;

import java.util.List;

public interface SourceRepository {
    int save(Source source);

    List<Source> findAll();

    List<Source> findEnabled();

    boolean deleteById(int id);

    boolean setEnabled(int id, boolean enabled);
}
