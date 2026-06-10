package com.pharmastock.repository;

import com.pharmastock.config.DatabaseConfig;
import com.pharmastock.model.InventoryTransaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventoryTransactionRepository extends AbstractRepository<InventoryTransaction> implements IInventoryTransactionRepository {

    public InventoryTransactionRepository(DatabaseConfig db) {
        super(db);
    }

    public List<InventoryTransaction> findAll() {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT it.*, m.medicine_name, m.medicine_code FROM inventory_transactions it JOIN medicines m ON it.medicine_id = m.medicine_id ORDER BY it.transaction_date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error findAll transactions: " + e.getMessage());
        }
        return transactions;
    }

    public List<InventoryTransaction> findRecent(int limit) {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT it.*, m.medicine_name, m.medicine_code FROM inventory_transactions it JOIN medicines m ON it.medicine_id = m.medicine_id ORDER BY it.transaction_date DESC LIMIT ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findRecent: " + e.getMessage());
        }
        return transactions;
    }

    public List<InventoryTransaction> findByMedicineId(int medicineId) {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT it.*, m.medicine_name, m.medicine_code FROM inventory_transactions it JOIN medicines m ON it.medicine_id = m.medicine_id WHERE it.medicine_id = ? ORDER BY it.transaction_date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findByMedicineId: " + e.getMessage());
        }
        return transactions;
    }

    public List<InventoryTransaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT it.*, m.medicine_name, m.medicine_code FROM inventory_transactions it JOIN medicines m ON it.medicine_id = m.medicine_id WHERE it.transaction_date BETWEEN ? AND ? ORDER BY it.transaction_date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findByDateRange: " + e.getMessage());
        }
        return transactions;
    }

    public int save(InventoryTransaction transaction) {
        String sql = "INSERT INTO inventory_transactions (medicine_id, transaction_type, quantity, transaction_date, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getMedicineId());
            ps.setString(2, transaction.getTransactionType());
            ps.setInt(3, transaction.getQuantity());
            if (transaction.getTransactionDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(transaction.getTransactionDate()));
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setString(5, transaction.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error save transaction: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Returns monthly OUT quantities for a medicine over the last N months.
     * Key: "YYYY-MM", Value: total OUT quantity.
     */
    public Map<String, Integer> getMonthlyOutMovement(int medicineId, int months) {
        Map<String, Integer> movement = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') as month_key, SUM(quantity) as total_qty " +
                "FROM inventory_transactions " +
                "WHERE medicine_id = ? AND transaction_type = 'OUT' " +
                "AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "GROUP BY month_key ORDER BY month_key";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movement.put(rs.getString("month_key"), rs.getInt("total_qty"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getMonthlyOutMovement: " + e.getMessage());
        }
        return movement;
    }

    /**
     * Counts total OUT transactions for a medicine in the last N days.
     */
    public int countOutTransactions(int medicineId, int days) {
        String sql = "SELECT COUNT(*) FROM inventory_transactions WHERE medicine_id = ? AND transaction_type = 'OUT' AND transaction_date >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error countOutTransactions: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Sum of OUT quantities for a medicine in the last N days.
     */
    public int sumOutQuantity(int medicineId, int days) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM inventory_transactions WHERE medicine_id = ? AND transaction_type = 'OUT' AND transaction_date >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error sumOutQuantity: " + e.getMessage());
        }
        return 0;
    }

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setMedicineId(rs.getInt("medicine_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setQuantity(rs.getInt("quantity"));
        Timestamp date = rs.getTimestamp("transaction_date");
        if (date != null) t.setTransactionDate(date.toLocalDateTime());
        t.setNotes(rs.getString("notes"));
        try {
            t.setMedicineName(rs.getString("medicine_name"));
            t.setMedicineCode(rs.getString("medicine_code"));
        } catch (SQLException ignored) {
        }
        return t;
    }

    @Override
    public java.util.Optional<InventoryTransaction> findById(int id) {
        String sql = "SELECT it.*, m.medicine_name, m.medicine_code FROM inventory_transactions it JOIN medicines m ON it.medicine_id = m.medicine_id WHERE it.transaction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findById transaction: " + e.getMessage());
        }
        return java.util.Optional.empty();
    }

    @Override
    public boolean update(InventoryTransaction entity) {
        throw new UnsupportedOperationException("Update transaction not supported");
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM inventory_transactions WHERE transaction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error delete transaction: " + e.getMessage());
        }
        return false;
    }
}
