package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.ShelterStock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShelterStockRepository {
    private final DatabaseConfig dbConfig;

    public ShelterStockRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<ShelterStock> findByShelterId(int shelterId) {
        List<ShelterStock> list = new ArrayList<>();
        String sql = "SELECT ss.*, m.medicine_name, m.medicine_code, m.category, m.unit " +
                     "FROM shelter_stocks ss " +
                     "JOIN medicines m ON ss.medicine_id = m.medicine_id " +
                     "WHERE ss.shelter_id = ? " +
                     "ORDER BY m.medicine_name ASC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStock(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ShelterStock> findAllCritical() {
        List<ShelterStock> list = new ArrayList<>();
        String sql = "SELECT ss.*, m.medicine_name, m.medicine_code, m.category, m.unit " +
                     "FROM shelter_stocks ss " +
                     "JOIN medicines m ON ss.medicine_id = m.medicine_id " +
                     "WHERE ss.quantity <= ss.minimum_stock " +
                     "ORDER BY ss.shelter_id ASC, m.medicine_name ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToStock(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ShelterStock findByShelterAndMedicine(int shelterId, int medicineId) {
        String sql = "SELECT ss.*, m.medicine_name, m.medicine_code, m.category, m.unit " +
                     "FROM shelter_stocks ss " +
                     "JOIN medicines m ON ss.medicine_id = m.medicine_id " +
                     "WHERE ss.shelter_id = ? AND ss.medicine_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            ps.setInt(2, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStock(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStock(int shelterId, int medicineId, int qtyChange) {
        String sql = "INSERT INTO shelter_stocks (shelter_id, medicine_id, quantity, minimum_stock, updated_at) " +
                     "VALUES (?, ?, GREATEST(0, ?), 10, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (shelter_id, medicine_id) DO UPDATE " +
                     "SET quantity = GREATEST(0, shelter_stocks.quantity + EXCLUDED.quantity), " +
                     "    updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            ps.setInt(2, medicineId);
            ps.setInt(3, qtyChange);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save(ShelterStock stock) {
        String sql = "INSERT INTO shelter_stocks (shelter_id, medicine_id, quantity, minimum_stock, updated_at) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (shelter_id, medicine_id) DO UPDATE " +
                     "SET quantity = EXCLUDED.quantity, " +
                     "    minimum_stock = EXCLUDED.minimum_stock, " +
                     "    updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock.getShelterId());
            ps.setInt(2, stock.getMedicineId());
            ps.setInt(3, stock.getQuantity());
            ps.setInt(4, stock.getMinimumStock());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ShelterStock mapResultSetToStock(ResultSet rs) throws SQLException {
        ShelterStock s = new ShelterStock();
        s.setShelterStockId(rs.getInt("shelter_stock_id"));
        s.setShelterId(rs.getInt("shelter_id"));
        s.setMedicineId(rs.getInt("medicine_id"));
        s.setQuantity(rs.getInt("quantity"));
        s.setMinimumStock(rs.getInt("minimum_stock"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        s.setMedicineName(rs.getString("medicine_name"));
        s.setMedicineCode(rs.getString("medicine_code"));
        s.setCategory(rs.getString("category"));
        s.setUnit(rs.getString("unit"));
        return s;
    }
}
