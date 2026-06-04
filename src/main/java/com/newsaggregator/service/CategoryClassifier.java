package com.newsaggregator.service;

import com.newsaggregator.model.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryClassifier {
    private final Map<Category, List<String>> keywordsByCategory;

    public CategoryClassifier() {
        keywordsByCategory = new HashMap<>();
        keywordsByCategory.put(Category.POLITICS, List.of(
                "президент", "выбор", "парламент", "депутат", "правительств", "санкци",
                "министр", "политик", "закон", "переговор", "дипломат"));
        keywordsByCategory.put(Category.ECONOMY, List.of(
                "экономик", "рубл", "доллар", "инфляц", "банк", "бирж", "налог",
                "бюджет", "цен", "рынок", "нефт", "газ", "акци", "инвест"));
        keywordsByCategory.put(Category.SPORT, List.of(
                "матч", "чемпионат", "футбол", "хоккей", "олимп", "турнир", "сборн",
                "гол", "спортсмен", "тренер", "медал"));
        keywordsByCategory.put(Category.SCIENCE, List.of(
                "учён", "ученые", "исследован", "наук", "открыт", "эксперимент",
                "космос", "физик", "биолог", "вакцин", "генетик"));
        keywordsByCategory.put(Category.TECH, List.of(
                "технолог", "смартфон", "компьютер", "интернет", "приложени", "softwar",
                "искусственн", "нейросет", "робот", "гаджет", "процессор"));
        keywordsByCategory.put(Category.CULTURE, List.of(
                "фильм", "кино", "театр", "музык", "выставк", "концерт", "артист",
                "писател", "роман", "премьер", "фестивал"));
        keywordsByCategory.put(Category.SOCIETY, List.of(
                "общество", "школ", "здравоохранен", "пенси", "семь", "город",
                "житель", "происшестви", "пожар", "авари", "погод"));
    }

    public Category classify(String text) {
        if (text == null || text.isBlank()) {
            return Category.OTHER;
        }
        String lower = text.toLowerCase();
        Category best = Category.OTHER;
        int bestScore = 0;
        for (Map.Entry<Category, List<String>> entry : keywordsByCategory.entrySet()) {
            int score = countMatches(lower, entry.getValue());
            if (score > bestScore) {
                bestScore = score;
                best = entry.getKey();
            }
        }
        return best;
    }

    private int countMatches(String text, List<String> keywords) {
        int score = 0;
        for (String keyword : keywords) {
            int index = 0;
            while ((index = text.indexOf(keyword, index)) >= 0) {
                score++;
                index += keyword.length();
            }
        }
        return score;
    }
}
