package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineRepository {

    private final DatabaseConfig dbConfig;

    public MedicineRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Medicine> findAll() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT m.medicine_id, m.medicine_code, m.medicine_name, m.category, m.batch_number, m.unit, m.stock_quantity, m.minimum_stock, m.purchase_price, m.selling_price, m.expiry_date, m.supplier_id, m.created_at, s.supplier_name " +
                     "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id ORDER BY m.medicine_name ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToMedicine(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Medicine findById(int medicineId) {
        String sql = "SELECT m.medicine_id, m.medicine_code, m.medicine_name, m.category, m.batch_number, m.unit, m.stock_quantity, m.minimum_stock, m.purchase_price, m.selling_price, m.expiry_date, m.supplier_id, m.created_at, s.supplier_name " +
                     "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.medicine_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Medicine findByCode(String medicineCode) {
        String sql = "SELECT m.medicine_id, m.medicine_code, m.medicine_name, m.category, m.batch_number, m.unit, m.stock_quantity, m.minimum_stock, m.purchase_price, m.selling_price, m.expiry_date, m.supplier_id, m.created_at, s.supplier_name " +
                     "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.medicine_code = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicineCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Medicine medicine) {
        if (medicine.getMedicineId() > 0) {
            String sql = "UPDATE medicines SET medicine_code = ?, medicine_name = ?, category = ?, batch_number = ?, unit = ?, stock_quantity = ?, minimum_stock = ?, purchase_price = ?, selling_price = ?, expiry_date = ?, supplier_id = ? WHERE medicine_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, medicine.getMedicineCode());
                ps.setString(2, medicine.getMedicineName());
                ps.setString(3, medicine.getCategory());
                ps.setString(4, medicine.getBatchNumber());
                ps.setString(5, medicine.getUnit());
                ps.setInt(6, medicine.getStockQuantity());
                ps.setInt(7, medicine.getMinimumStock());
                ps.setDouble(8, medicine.getPurchasePrice());
                ps.setDouble(9, medicine.getSellingPrice());
                ps.setDate(10, medicine.getExpiryDate());
                if (medicine.getSupplierId() != null) {
                    ps.setInt(11, medicine.getSupplierId());
                } else {
                    ps.setNull(11, Types.INTEGER);
                }
                ps.setInt(12, medicine.getMedicineId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO medicines (medicine_code, medicine_name, category, batch_number, unit, stock_quantity, minimum_stock, purchase_price, selling_price, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, medicine.getMedicineCode());
                ps.setString(2, medicine.getMedicineName());
                ps.setString(3, medicine.getCategory());
                ps.setString(4, medicine.getBatchNumber());
                ps.setString(5, medicine.getUnit());
                ps.setInt(6, medicine.getStockQuantity());
                ps.setInt(7, medicine.getMinimumStock());
                ps.setDouble(8, medicine.getPurchasePrice());
                ps.setDouble(9, medicine.getSellingPrice());
                ps.setDate(10, medicine.getExpiryDate());
                if (medicine.getSupplierId() != null) {
                    ps.setInt(11, medicine.getSupplierId());
                } else {
                    ps.setNull(11, Types.INTEGER);
                }
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            medicine.setMedicineId(rs.getInt(1));
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

    public boolean updateStock(int medicineId, int qtyChange) {
        String sql = "UPDATE medicines SET stock_quantity = GREATEST(0, stock_quantity + ?) WHERE medicine_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qtyChange);
            ps.setInt(2, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setMedicineCode(rs.getString("medicine_code"));
        m.setMedicineName(rs.getString("medicine_name"));
        m.setCategory(rs.getString("category"));
        m.setBatchNumber(rs.getString("batch_number"));
        m.setUnit(rs.getString("unit"));
        m.setStockQuantity(rs.getInt("stock_quantity"));
        m.setMinimumStock(rs.getInt("minimum_stock"));
        m.setPurchasePrice(rs.getDouble("purchase_price"));
        m.setSellingPrice(rs.getDouble("selling_price"));
        m.setExpiryDate(rs.getDate("expiry_date"));
        int supId = rs.getInt("supplier_id");
        if (rs.wasNull()) {
            m.setSupplierId(null);
        } else {
            m.setSupplierId(supId);
        }
        m.setCreatedAt(rs.getTimestamp("created_at"));
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }
}
