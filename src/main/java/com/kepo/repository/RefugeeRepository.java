package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Refugee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefugeeRepository {

    private final DatabaseConfig dbConfig;

    public RefugeeRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Refugee> findAll() {
        List<Refugee> list = new ArrayList<>();
        String sql = "SELECT r.refugee_id, r.name, r.nik, r.age, r.gender, r.status, r.medical_notes, r.shelter_id, r.check_in_time, r.check_out_time, r.created_at, s.name AS shelter_name " +
                     "FROM refugees r LEFT JOIN shelters s ON r.shelter_id = s.shelter_id ORDER BY r.created_at DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToRefugee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Refugee findById(int refugeeId) {
        String sql = "SELECT r.refugee_id, r.name, r.nik, r.age, r.gender, r.status, r.medical_notes, r.shelter_id, r.check_in_time, r.check_out_time, r.created_at, s.name AS shelter_name " +
                     "FROM refugees r LEFT JOIN shelters s ON r.shelter_id = s.shelter_id WHERE r.refugee_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, refugeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRefugee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Refugee findByNik(String nik) {
        String sql = "SELECT r.refugee_id, r.name, r.nik, r.age, r.gender, r.status, r.medical_notes, r.shelter_id, r.check_in_time, r.check_out_time, r.created_at, s.name AS shelter_name " +
                     "FROM refugees r LEFT JOIN shelters s ON r.shelter_id = s.shelter_id WHERE r.nik = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRefugee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Refugee> findByShelter(int shelterId) {
        List<Refugee> list = new ArrayList<>();
        String sql = "SELECT r.refugee_id, r.name, r.nik, r.age, r.gender, r.status, r.medical_notes, r.shelter_id, r.check_in_time, r.check_out_time, r.created_at, s.name AS shelter_name " +
                     "FROM refugees r LEFT JOIN shelters s ON r.shelter_id = s.shelter_id WHERE r.shelter_id = ? ORDER BY r.name ASC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRefugee(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean save(Refugee refugee) {
        if (refugee.getRefugeeId() > 0) {
            String sql = "UPDATE refugees SET name = ?, nik = ?, age = ?, gender = ?, status = ?, medical_notes = ?, shelter_id = ?, check_in_time = ?, check_out_time = ? WHERE refugee_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, refugee.getName());
                ps.setString(2, refugee.getNik());
                ps.setInt(3, refugee.getAge());
                ps.setString(4, refugee.getGender());
                ps.setString(5, refugee.getStatus());
                ps.setString(6, refugee.getMedicalNotes());
                if (refugee.getShelterId() != null) {
                    ps.setInt(7, refugee.getShelterId());
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                ps.setTimestamp(8, refugee.getCheckInTime());
                ps.setTimestamp(9, refugee.getCheckOutTime());
                ps.setInt(10, refugee.getRefugeeId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO refugees (name, nik, age, gender, status, medical_notes, shelter_id, check_in_time, check_out_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, refugee.getName());
                ps.setString(2, refugee.getNik());
                ps.setInt(3, refugee.getAge());
                ps.setString(4, refugee.getGender());
                ps.setString(5, refugee.getStatus());
                ps.setString(6, refugee.getMedicalNotes());
                if (refugee.getShelterId() != null) {
                    ps.setInt(7, refugee.getShelterId());
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                ps.setTimestamp(8, refugee.getCheckInTime());
                ps.setTimestamp(9, refugee.getCheckOutTime());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            refugee.setRefugeeId(rs.getInt(1));
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

    public boolean delete(int refugeeId) {
        String sql = "DELETE FROM refugees WHERE refugee_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, refugeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Refugee mapResultSetToRefugee(ResultSet rs) throws SQLException {
        Refugee r = new Refugee();
        r.setRefugeeId(rs.getInt("refugee_id"));
        r.setName(rs.getString("name"));
        r.setNik(rs.getString("nik"));
        r.setAge(rs.getInt("age"));
        r.setGender(rs.getString("gender"));
        r.setStatus(rs.getString("status"));
        r.setMedicalNotes(rs.getString("medical_notes"));
        int shelterId = rs.getInt("shelter_id");
        if (rs.wasNull()) {
            r.setShelterId(null);
        } else {
            r.setShelterId(shelterId);
        }
        r.setCheckInTime(rs.getTimestamp("check_in_time"));
        r.setCheckOutTime(rs.getTimestamp("check_out_time"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setShelterName(rs.getString("shelter_name"));
        return r;
    }
}
