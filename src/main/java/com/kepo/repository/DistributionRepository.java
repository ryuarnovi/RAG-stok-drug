package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Distribution;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistributionRepository {

    private final DatabaseConfig dbConfig;

    public DistributionRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Distribution> findAll() {
        List<Distribution> list = new ArrayList<>();
        String sql = "SELECT d.distribution_id, d.doc_num, d.shelter_id, d.item_type, d.quantity, d.status, d.notes, d.created_at, s.name AS shelter_name " +
                     "FROM distributions d LEFT JOIN shelters s ON d.shelter_id = s.shelter_id ORDER BY d.created_at DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDistribution(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Distribution findById(int distributionId) {
        String sql = "SELECT d.distribution_id, d.doc_num, d.shelter_id, d.item_type, d.quantity, d.status, d.notes, d.created_at, s.name AS shelter_name " +
                     "FROM distributions d LEFT JOIN shelters s ON d.shelter_id = s.shelter_id WHERE d.distribution_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, distributionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDistribution(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Distribution findByDocNum(String docNum) {
        String sql = "SELECT d.distribution_id, d.doc_num, d.shelter_id, d.item_type, d.quantity, d.status, d.notes, d.created_at, s.name AS shelter_name " +
                     "FROM distributions d LEFT JOIN shelters s ON d.shelter_id = s.shelter_id WHERE d.doc_num = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDistribution(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Distribution d) {
        if (d.getDistributionId() > 0) {
            String sql = "UPDATE distributions SET doc_num = ?, shelter_id = ?, item_type = ?, quantity = ?, status = ?, notes = ? WHERE distribution_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, d.getDocNum());
                ps.setInt(2, d.getShelterId());
                ps.setString(3, d.getItemType());
                ps.setInt(4, d.getQuantity());
                ps.setString(5, d.getStatus());
                ps.setString(6, d.getNotes());
                ps.setInt(7, d.getDistributionId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO distributions (doc_num, shelter_id, item_type, quantity, status, notes) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, d.getDocNum());
                ps.setInt(2, d.getShelterId());
                ps.setString(3, d.getItemType());
                ps.setInt(4, d.getQuantity());
                ps.setString(5, d.getStatus());
                ps.setString(6, d.getNotes());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            d.setDistributionId(rs.getInt(1));
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

    public boolean delete(int distributionId) {
        String sql = "DELETE FROM distributions WHERE distribution_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, distributionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Distribution mapResultSetToDistribution(ResultSet rs) throws SQLException {
        Distribution d = new Distribution();
        d.setDistributionId(rs.getInt("distribution_id"));
        d.setDocNum(rs.getString("doc_num"));
        d.setShelterId(rs.getInt("shelter_id"));
        d.setItemType(rs.getString("item_type"));
        d.setQuantity(rs.getInt("quantity"));
        d.setStatus(rs.getString("status"));
        d.setNotes(rs.getString("notes"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        d.setShelterName(rs.getString("shelter_name"));
        return d;
    }
}
