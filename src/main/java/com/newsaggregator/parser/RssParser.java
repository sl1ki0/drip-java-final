package com.newsaggregator.parser;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Source;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RssParser implements NewsParser {
    private final int timeoutMs;

    public RssParser() {
        this(10000);
    }

    public RssParser(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public List<Article> fetchArticles(Source source) {
        try {
            Document doc = JsoupSupport.connect(source.getRssUrl(), timeoutMs)
                    .parser(Parser.xmlParser())
                    .get();
            return parse(doc, source);
        } catch (IOException e) {
            throw new ParsingException("Cannot load RSS: " + source.getRssUrl(), e);
        }
    }

    public List<Article> parse(String xml, Source source) {
        return parse(Jsoup.parse(xml, "", Parser.xmlParser()), source);
    }

    private List<Article> parse(Document doc, Source source) {
        List<Article> articles = new ArrayList<>();
        Elements items = doc.select("item");
        if (items.isEmpty()) {
            items = doc.select("entry");
        }
        for (Element item : items) {
            Article article = toArticle(item, source);
            if (article != null) {
                articles.add(article);
            }
        }
        return articles;
    }

    private Article toArticle(Element item, Source source) {
        String link = text(item, "link");
        if (link.isBlank()) {
            Element linkEl = item.selectFirst("link");
            if (linkEl != null) {
                link = linkEl.attr("href");
            }
        }
        String title = text(item, "title");
        if (link.isBlank() || title.isBlank()) {
            return null;
        }

        Article article = new Article();
        article.setSourceId(source.getId());
        article.setSourceName(source.getName());
        article.setUrl(link.trim());
        article.setTitle(title.trim());
        article.setSummary(stripHtml(firstNonBlank(text(item, "description"), text(item, "summary"))));
        article.setPublishedAt(parseDate(firstNonBlank(text(item, "pubDate"), text(item, "published"), text(item, "updated"))));
        article.setMediaLinks(extractMedia(item));
        return article;
    }

    private List<String> extractMedia(Element item) {
        List<String> media = new ArrayList<>();
        for (Element el : item.select("enclosure[url]")) {
            media.add(el.attr("url"));
        }
        for (Element el : item.select("media|content[url], media|thumbnail[url]")) {
            media.add(el.attr("url"));
        }
        return media;
    }

    private String text(Element parent, String tag) {
        Element el = parent.selectFirst(tag);
        return el == null ? "" : el.text();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parse(html).text();
    }

    private Instant parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        try {
            return DateTimeFormatter.RFC_1123_DATE_TIME.parse(value, Instant::from);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return DateTimeFormatter.ISO_DATE_TIME.parse(value, Instant::from);
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
