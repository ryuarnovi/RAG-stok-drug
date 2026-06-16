package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.RefugeeMovement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefugeeMovementRepository {
    private final DatabaseConfig dbConfig;

    public RefugeeMovementRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public boolean logMovement(int refugeeId, Integer fromShelterId, Integer toShelterId, String movedBy, String notes) {
        String sql = "INSERT INTO refugee_movements (refugee_id, from_shelter_id, to_shelter_id, moved_by, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, refugeeId);
            if (fromShelterId != null) {
                ps.setInt(2, fromShelterId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (toShelterId != null) {
                ps.setInt(3, toShelterId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, movedBy);
            ps.setString(5, notes);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RefugeeMovement> findAll() {
        List<RefugeeMovement> list = new ArrayList<>();
        String sql = "SELECT rm.*, r.name AS refugee_name, s1.name AS from_name, s2.name AS to_name " +
                     "FROM refugee_movements rm " +
                     "JOIN refugees r ON rm.refugee_id = r.refugee_id " +
                     "LEFT JOIN shelters s1 ON rm.from_shelter_id = s1.shelter_id " +
                     "LEFT JOIN shelters s2 ON rm.to_shelter_id = s2.shelter_id " +
                     "ORDER BY rm.moved_at DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToMovement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RefugeeMovement> findByRefugeeId(int refugeeId) {
        List<RefugeeMovement> list = new ArrayList<>();
        String sql = "SELECT rm.*, r.name AS refugee_name, s1.name AS from_name, s2.name AS to_name " +
                     "FROM refugee_movements rm " +
                     "JOIN refugees r ON rm.refugee_id = r.refugee_id " +
                     "LEFT JOIN shelters s1 ON rm.from_shelter_id = s1.shelter_id " +
                     "LEFT JOIN shelters s2 ON rm.to_shelter_id = s2.shelter_id " +
                     "WHERE rm.refugee_id = ? " +
                     "ORDER BY rm.moved_at DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, refugeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovement(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private RefugeeMovement mapResultSetToMovement(ResultSet rs) throws SQLException {
        RefugeeMovement m = new RefugeeMovement();
        m.setMovementId(rs.getInt("movement_id"));
        m.setRefugeeId(rs.getInt("refugee_id"));
        int fromId = rs.getInt("from_shelter_id");
        if (rs.wasNull()) {
            m.setFromShelterId(null);
        } else {
            m.setFromShelterId(fromId);
        }
        int toId = rs.getInt("to_shelter_id");
        if (rs.wasNull()) {
            m.setToShelterId(null);
        } else {
            m.setToShelterId(toId);
        }
        m.setMovedBy(rs.getString("moved_by"));
        m.setNotes(rs.getString("notes"));
        m.setMovedAt(rs.getTimestamp("moved_at"));
        m.setRefugeeName(rs.getString("refugee_name"));
        m.setFromShelterName(rs.getString("from_name"));
        m.setToShelterName(rs.getString("to_name"));
        return m;
    }
}
