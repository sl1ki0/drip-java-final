package com.newsaggregator.repository;

import com.newsaggregator.model.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteSourceRepository implements SourceRepository {
    private final Connection connection;

    public SqliteSourceRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int save(Source source) {
        String sql = "INSERT OR IGNORE INTO sources (name, rss_url, enabled) VALUES (?, ?, ?)";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, source.getName());
                ps.setString(2, source.getRssUrl());
                ps.setInt(3, source.isEnabled() ? 1 : 0);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        source.setId(id);
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
    public List<Source> findAll() {
        return query("SELECT * FROM sources ORDER BY id");
    }

    @Override
    public List<Source> findEnabled() {
        return query("SELECT * FROM sources WHERE enabled = 1 ORDER BY id");
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM sources WHERE id = ?";
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
    public boolean setEnabled(int id, boolean enabled) {
        String sql = "UPDATE sources SET enabled = ? WHERE id = ?";
        synchronized (connection) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, enabled ? 1 : 0);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Source> query(String sql) {
        List<Source> result = new ArrayList<>();
        synchronized (connection) {
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    result.add(new Source(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("rss_url"),
                            rs.getInt("enabled") == 1));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
