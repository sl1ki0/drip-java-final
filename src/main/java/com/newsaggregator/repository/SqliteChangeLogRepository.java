package com.newsaggregator.repository;

import com.newsaggregator.model.ChangeLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteChangeLogRepository implements ChangeLogRepository {
    private final Connection connection;

    public SqliteChangeLogRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(ChangeLogEntry entry) {
        String sql = "INSERT INTO change_log (action, article_id, timestamp, details) VALUES (?, ?, ?, ?)";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, entry.getAction());
                if (entry.getArticleId() == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, entry.getArticleId());
                }
                ps.setString(3, entry.getTimestamp().toString());
                ps.setString(4, entry.getDetails());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<ChangeLogEntry> findRecent(int limit) {
        String sql = "SELECT * FROM change_log ORDER BY id DESC LIMIT ?";
        List<ChangeLogEntry> result = new ArrayList<>();
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int articleId = rs.getInt("article_id");
                        Integer boxedArticleId = rs.wasNull() ? null : articleId;
                        ChangeLogEntry entry = new ChangeLogEntry(
                                rs.getString("action"),
                                boxedArticleId,
                                Instant.parse(rs.getString("timestamp")),
                                rs.getString("details"));
                        entry.setId(rs.getInt("id"));
                        result.add(entry);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
