package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {

    private final DatabaseConfig dbConfig;

    public AuditLogRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT log_id, username, action, details, created_at FROM audit_logs ORDER BY created_at DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUsername(rs.getString("username"));
                log.setAction(rs.getString("action"));
                log.setDetails(rs.getString("details"));
                log.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (username, action, details) VALUES (?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDetails());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setLogId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
