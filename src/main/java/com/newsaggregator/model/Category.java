package com.newsaggregator.model;

public enum Category {
    POLITICS("Политика"),
    ECONOMY("Экономика"),
    SPORT("Спорт"),
    SCIENCE("Наука"),
    TECH("Технологии"),
    CULTURE("Культура"),
    SOCIETY("Общество"),
    OTHER("Прочее");

    private final String title;

    Category(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
