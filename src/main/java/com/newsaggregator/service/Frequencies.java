package com.newsaggregator.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

final class Frequencies {
    private Frequencies() {
    }

    static <K> LinkedHashMap<K, Integer> top(Map<K, Integer> frequency, int limit) {
        return frequency.entrySet().stream()
                .sorted(Comparator.<Map.Entry<K, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .limit(limit)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }
}
