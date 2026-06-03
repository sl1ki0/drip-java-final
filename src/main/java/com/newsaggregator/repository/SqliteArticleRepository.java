package com.newsaggregator.repository;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SqliteArticleRepository implements ArticleRepository {
    private static final String LIST_SEPARATOR = "\n";
    private final Connection connection;

    public SqliteArticleRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean existsByUrl(String url) {
        String sql = "SELECT 1 FROM articles WHERE url = ? LIMIT 1";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, url);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int save(Article article) {
        String sql = "INSERT INTO articles "
                + "(source_id, url, title, published_at, summary, content, category, keywords, media_links, fetched_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, article.getSourceId());
                ps.setString(2, article.getUrl());
                ps.setString(3, article.getTitle());
                ps.setString(4, toText(article.getPublishedAt()));
                ps.setString(5, article.getSummary());
                ps.setString(6, article.getContent());
                ps.setString(7, article.getCategory() == null ? Category.OTHER.name() : article.getCategory().name());
                ps.setString(8, joinList(article.getKeywords()));
                ps.setString(9, joinList(article.getMediaLinks()));
                ps.setString(10, toText(article.getFetchedAt()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        article.setId(id);
                        return id;
                    }
                }
                return 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Article> findAll() {
        String sql = "SELECT a.*, s.name AS source_name FROM articles a "
                + "LEFT JOIN sources s ON a.source_id = s.id ORDER BY a.published_at DESC";
        List<Article> result = new ArrayList<>();
        synchronized (connection) {
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public Optional<Article> findById(int id) {
        String sql = "SELECT a.*, s.name AS source_name FROM articles a "
                + "LEFT JOIN sources s ON a.source_id = s.id WHERE a.id = ?";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(map(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM articles WHERE id = ?";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Article> findOlderThanDays(int days) {
        Instant threshold = Instant.now().minus(Duration.ofDays(days));
        List<Article> result = new ArrayList<>();
        for (Article a : findAll()) {
            if (a.getPublishedAt() != null && a.getPublishedAt().isBefore(threshold)) {
                result.add(a);
            }
        }
        return result;
    }

    private Article map(ResultSet rs) throws SQLException {
        Article a = new Article();
        a.setId(rs.getInt("id"));
        a.setSourceId(rs.getInt("source_id"));
        a.setSourceName(rs.getString("source_name"));
        a.setUrl(rs.getString("url"));
        a.setTitle(rs.getString("title"));
        a.setPublishedAt(parseInstant(rs.getString("published_at")));
        a.setSummary(rs.getString("summary"));
        a.setContent(rs.getString("content"));
        a.setCategory(parseCategory(rs.getString("category")));
        a.setKeywords(splitList(rs.getString("keywords")));
        a.setMediaLinks(splitList(rs.getString("media_links")));
        a.setFetchedAt(parseInstant(rs.getString("fetched_at")));
        return a;
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(LIST_SEPARATOR, values);
    }

    private List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(LIST_SEPARATOR)));
    }

    private String toText(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Instant.parse(value);
    }

    private Category parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return Category.OTHER;
        }
        try {
            return Category.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Category.OTHER;
        }
    }
}
