package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.InventoryTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionRepository {

    private final DatabaseConfig dbConfig;

    public InventoryTransactionRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<InventoryTransaction> findAll() {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.transaction_id, t.medicine_id, t.transaction_type, t.quantity, t.transaction_date, t.notes, m.medicine_name " +
                     "FROM inventory_transactions t LEFT JOIN medicines m ON t.medicine_id = m.medicine_id ORDER BY t.transaction_date DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<InventoryTransaction> findByMedicineId(int medicineId) {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.transaction_id, t.medicine_id, t.transaction_type, t.quantity, t.transaction_date, t.notes, m.medicine_name " +
                     "FROM inventory_transactions t LEFT JOIN medicines m ON t.medicine_id = m.medicine_id WHERE t.medicine_id = ? ORDER BY t.transaction_date DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean save(InventoryTransaction t) {
        String sql = "INSERT INTO inventory_transactions (medicine_id, transaction_type, quantity, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getMedicineId());
            ps.setString(2, t.getTransactionType());
            ps.setInt(3, t.getQuantity());
            ps.setString(4, t.getNotes());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        t.setTransactionId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private InventoryTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setMedicineId(rs.getInt("medicine_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setQuantity(rs.getInt("quantity"));
        t.setTransactionDate(rs.getTimestamp("transaction_date"));
        t.setNotes(rs.getString("notes"));
        t.setMedicineName(rs.getString("medicine_name"));
        return t;
    }
}
