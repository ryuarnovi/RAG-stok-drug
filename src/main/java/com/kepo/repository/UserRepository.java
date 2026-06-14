package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final DatabaseConfig dbConfig;

    public UserRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, full_name, role, created_at FROM users WHERE username = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, role, created_at FROM users ORDER BY username ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean save(User user) {
        if (user.getUserId() > 0) {
            String sql = "UPDATE users SET password_hash = ?, full_name = ?, role = ? WHERE user_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getPasswordHash());
                ps.setString(2, user.getFullName());
                ps.setString(3, user.getRole().name());
                ps.setInt(4, user.getUserId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole().name());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            user.setUserId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
