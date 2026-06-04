package com.newsaggregator.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeywordExtractor {
    private static final Set<String> STOP_WORDS = Set.of(
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как", "а", "то",
            "все", "она", "так", "его", "но", "да", "ты", "к", "у", "же", "вы", "за",
            "бы", "по", "только", "ее", "мне", "было", "вот", "от", "меня", "о", "из",
            "ему", "теперь", "когда", "даже", "ну", "вдруг", "ли", "если", "уже", "или",
            "быть", "был", "него", "до", "вас", "нибудь", "опять", "уж", "вам", "ведь",
            "там", "потом", "себя", "ничего", "ей", "может", "они", "тут", "где", "есть",
            "над", "для", "это", "этот", "эта", "при", "об", "the", "a", "an", "of", "to");

    public List<String> extractKeywords(String text, int limit) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String[] tokens = text.toLowerCase().split("[^\\p{L}]+");
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String token : tokens) {
            if (token.length() < 4 || STOP_WORDS.contains(token)) {
                continue;
            }
            counts.merge(token, 1, Integer::sum);
        }
        return new ArrayList<>(Frequencies.top(counts, limit).keySet());
    }

    public String summarize(String text, int maxSentences) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (count > 0) {
                sb.append(' ');
            }
            sb.append(trimmed);
            count++;
            if (count >= maxSentences) {
                break;
            }
        }
        return sb.toString();
    }
}
