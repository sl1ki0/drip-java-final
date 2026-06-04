package com.newsaggregator.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newsaggregator.model.Article;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public class ExportService {
    private final Gson gson;

    public ExportService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class,
                        (com.google.gson.JsonSerializer<Instant>) (src, type, context) ->
                                src == null ? null : new com.google.gson.JsonPrimitive(src.toString()))
                .create();
    }

    public String export(List<Article> articles, ExportFormat format) {
        switch (format) {
            case JSON:
                return toJson(articles);
            case HTML:
                return toHtml(articles);
            case CSV:
            default:
                return toCsv(articles);
        }
    }

    public Path exportToFile(List<Article> articles, ExportFormat format, Path path) {
        try {
            Files.writeString(path, export(articles, format), StandardCharsets.UTF_8);
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toCsv(List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,source,url,title,publishedAt,category,keywords,summary\n");
        for (Article a : articles) {
            sb.append(escape(String.valueOf(a.getId()))).append(',')
                    .append(escape(a.getSourceName())).append(',')
                    .append(escape(a.getUrl())).append(',')
                    .append(escape(a.getTitle())).append(',')
                    .append(escape(a.getPublishedAt() == null ? "" : a.getPublishedAt().toString())).append(',')
                    .append(escape(a.getCategory() == null ? "" : a.getCategory().getTitle())).append(',')
                    .append(escape(String.join("; ", a.getKeywords()))).append(',')
                    .append(escape(a.getSummary())).append('\n');
        }
        return sb.toString();
    }

    public String toJson(List<Article> articles) {
        return gson.toJson(articles);
    }

    public String toHtml(List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"ru\">\n<head>\n<meta charset=\"UTF-8\">\n")
                .append("<title>Новости</title>\n</head>\n<body>\n")
                .append("<h1>Новости</h1>\n<table border=\"1\" cellpadding=\"5\">\n")
                .append("<tr><th>Дата</th><th>Источник</th><th>Категория</th><th>Заголовок</th></tr>\n");
        for (Article a : articles) {
            sb.append("<tr>")
                    .append("<td>").append(html(a.getPublishedAt() == null ? "" : a.getPublishedAt().toString())).append("</td>")
                    .append("<td>").append(html(a.getSourceName())).append("</td>")
                    .append("<td>").append(html(a.getCategory() == null ? "" : a.getCategory().getTitle())).append("</td>")
                    .append("<td><a href=\"").append(html(a.getUrl())).append("\">")
                    .append(html(a.getTitle())).append("</a></td>")
                    .append("</tr>\n");
        }
        sb.append("</table>\n</body>\n</html>\n");
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
        if (escaped.contains(",") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String html(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
