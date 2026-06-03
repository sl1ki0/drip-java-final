package com.newsaggregator.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ArticleScraper implements ContentEnricher {
    private static final String NOISE_SELECTOR =
            "script, style, nav, aside, header, footer, form, noscript, "
            + ".ad, .ads, .advert, .banner, .social, .share, .comments, .related";
    private final int timeoutMs;

    public ArticleScraper() {
        this(10000);
    }

    public ArticleScraper(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public EnrichedContent enrich(String url) {
        try {
            Document doc = JsoupSupport.connect(url, timeoutMs).get();
            return new EnrichedContent(extractText(doc), extractMedia(doc));
        } catch (IOException e) {
            return EnrichedContent.empty();
        }
    }

    public String extractText(Document doc) {
        doc.select(NOISE_SELECTOR).remove();
        Element container = pickContainer(doc);
        StringBuilder sb = new StringBuilder();
        for (Element p : container.select("p")) {
            String text = p.text().trim();
            if (text.length() > 20) {
                sb.append(text).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    public List<String> extractMedia(Document doc) {
        Set<String> media = new LinkedHashSet<>();
        for (Element img : doc.select("img[src]")) {
            String src = img.absUrl("src");
            if (!src.isBlank()) {
                media.add(src);
            }
        }
        for (Element video : doc.select("video source[src], video[src]")) {
            String src = video.absUrl("src");
            if (!src.isBlank()) {
                media.add(src);
            }
        }
        return new ArrayList<>(media);
    }

    private Element pickContainer(Document doc) {
        Element article = doc.selectFirst("article");
        if (article != null) {
            return article;
        }
        Element main = doc.selectFirst("main");
        if (main != null) {
            return main;
        }
        return doc.body() != null ? doc.body() : doc;
    }
}
