package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.MedicineRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineRequestRepository {

    private final DatabaseConfig dbConfig;

    public MedicineRequestRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<MedicineRequest> findAll() {
        List<MedicineRequest> list = new ArrayList<>();
        String sql = "SELECT r.request_id, r.refugee_id, r.shelter_id, r.medicine_code, r.medicine_name, r.quantity, r.status, r.notes, r.created_at, "
                   + "ref.name AS refugee_name, sh.name AS shelter_name "
                   + "FROM medicine_requests r "
                   + "LEFT JOIN refugees ref ON r.refugee_id = ref.refugee_id "
                   + "LEFT JOIN shelters sh ON r.shelter_id = sh.shelter_id "
                   + "ORDER BY r.created_at DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MedicineRequest> findByShelterId(int shelterId) {
        List<MedicineRequest> list = new ArrayList<>();
        String sql = "SELECT r.request_id, r.refugee_id, r.shelter_id, r.medicine_code, r.medicine_name, r.quantity, r.status, r.notes, r.created_at, "
                   + "ref.name AS refugee_name, sh.name AS shelter_name "
                   + "FROM medicine_requests r "
                   + "LEFT JOIN refugees ref ON r.refugee_id = ref.refugee_id "
                   + "LEFT JOIN shelters sh ON r.shelter_id = sh.shelter_id "
                   + "WHERE r.shelter_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public MedicineRequest findById(int requestId) {
        String sql = "SELECT r.request_id, r.refugee_id, r.shelter_id, r.medicine_code, r.medicine_name, r.quantity, r.status, r.notes, r.created_at, "
                   + "ref.name AS refugee_name, sh.name AS shelter_name "
                   + "FROM medicine_requests r "
                   + "LEFT JOIN refugees ref ON r.refugee_id = ref.refugee_id "
                   + "LEFT JOIN shelters sh ON r.shelter_id = sh.shelter_id "
                   + "WHERE r.request_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(MedicineRequest req) {
        if (req.getRequestId() > 0) {
            String sql = "UPDATE medicine_requests SET refugee_id=?, shelter_id=?, medicine_code=?, medicine_name=?, quantity=?, status=?, notes=? WHERE request_id=?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, req.getRefugeeId());
                ps.setInt(2, req.getShelterId());
                ps.setString(3, req.getMedicineCode());
                ps.setString(4, req.getMedicineName());
                ps.setInt(5, req.getQuantity());
                ps.setString(6, req.getStatus());
                ps.setString(7, req.getNotes());
                ps.setInt(8, req.getRequestId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO medicine_requests (refugee_id, shelter_id, medicine_code, medicine_name, quantity, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, req.getRefugeeId());
                ps.setInt(2, req.getShelterId());
                ps.setString(3, req.getMedicineCode());
                ps.setString(4, req.getMedicineName());
                ps.setInt(5, req.getQuantity());
                ps.setString(6, req.getStatus() != null ? req.getStatus() : "PENDING");
                ps.setString(7, req.getNotes());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) req.setRequestId(rs.getInt(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean updateStatus(int requestId, String status, String notes) {
        String sql = "UPDATE medicine_requests SET status=?, notes=CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE notes END WHERE request_id=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setString(3, notes);
            ps.setString(4, notes);
            ps.setInt(5, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private MedicineRequest map(ResultSet rs) throws SQLException {
        MedicineRequest r = new MedicineRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setRefugeeId(rs.getInt("refugee_id"));
        r.setShelterId(rs.getInt("shelter_id"));
        r.setMedicineCode(rs.getString("medicine_code"));
        r.setMedicineName(rs.getString("medicine_name"));
        r.setQuantity(rs.getInt("quantity"));
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setRefugeeName(rs.getString("refugee_name"));
        r.setShelterName(rs.getString("shelter_name"));
        return r;
    }
}
