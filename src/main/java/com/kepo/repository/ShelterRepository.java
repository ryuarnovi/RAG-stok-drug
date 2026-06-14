package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Shelter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShelterRepository {

    private final DatabaseConfig dbConfig;

    public ShelterRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Shelter> findAll() {
        List<Shelter> list = new ArrayList<>();
        String sql = "SELECT shelter_id, name, location, capacity, current_occupancy, status, penanggung_jawab, created_at FROM shelters ORDER BY name ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToShelter(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Shelter findById(int shelterId) {
        String sql = "SELECT shelter_id, name, location, capacity, current_occupancy, status, penanggung_jawab, created_at FROM shelters WHERE shelter_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShelter(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Shelter shelter) {
        // Automatically determine status based on occupancy and capacity
        double occupancyRatio = shelter.getCapacity() > 0 ? (double) shelter.getCurrentOccupancy() / shelter.getCapacity() : 0.0;
        if (occupancyRatio >= 0.95) {
            shelter.setStatus("KRITIS");
        } else if (occupancyRatio >= 0.8) {
            shelter.setStatus("WASPADA");
        } else {
            shelter.setStatus("AMAN");
        }

        if (shelter.getShelterId() > 0) {
            String sql = "UPDATE shelters SET name = ?, location = ?, capacity = ?, current_occupancy = ?, status = ?, penanggung_jawab = ? WHERE shelter_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, shelter.getName());
                ps.setString(2, shelter.getLocation());
                ps.setInt(3, shelter.getCapacity());
                ps.setInt(4, shelter.getCurrentOccupancy());
                ps.setString(5, shelter.getStatus());
                ps.setString(6, shelter.getPenanggungJawab());
                ps.setInt(7, shelter.getShelterId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO shelters (name, location, capacity, current_occupancy, status, penanggung_jawab) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, shelter.getName());
                ps.setString(2, shelter.getLocation());
                ps.setInt(3, shelter.getCapacity());
                ps.setInt(4, shelter.getCurrentOccupancy());
                ps.setString(5, shelter.getStatus());
                ps.setString(6, shelter.getPenanggungJawab());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            shelter.setShelterId(rs.getInt(1));
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

    public boolean updateOccupancy(int shelterId, int change) {
        String sql = "UPDATE shelters SET current_occupancy = GREATEST(0, current_occupancy + ?) WHERE shelter_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, change);
            ps.setInt(2, shelterId);
            if (ps.executeUpdate() > 0) {
                // Now recalculate status
                Shelter s = findById(shelterId);
                if (s != null) {
                    return save(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int shelterId) {
        String sql = "DELETE FROM shelters WHERE shelter_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Shelter mapResultSetToShelter(ResultSet rs) throws SQLException {
        Shelter sh = new Shelter();
        sh.setShelterId(rs.getInt("shelter_id"));
        sh.setName(rs.getString("name"));
        sh.setLocation(rs.getString("location"));
        sh.setCapacity(rs.getInt("capacity"));
        sh.setCurrentOccupancy(rs.getInt("current_occupancy"));
        sh.setStatus(rs.getString("status"));
        sh.setPenanggungJawab(rs.getString("penanggung_jawab"));
        sh.setCreatedAt(rs.getTimestamp("created_at"));
        return sh;
    }
}
