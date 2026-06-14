package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository {

    private final DatabaseConfig dbConfig;

    public SupplierRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT supplier_id, supplier_name, contact_person, phone, email, address, created_at FROM suppliers ORDER BY supplier_name ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Supplier findById(int supplierId) {
        String sql = "SELECT supplier_id, supplier_name, contact_person, phone, email, address, created_at FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Supplier supplier) {
        if (supplier.getSupplierId() > 0) {
            String sql = "UPDATE suppliers SET supplier_name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, supplier.getSupplierName());
                ps.setString(2, supplier.getContactPerson());
                ps.setString(3, supplier.getPhone());
                ps.setString(4, supplier.getEmail());
                ps.setString(5, supplier.getAddress());
                ps.setInt(6, supplier.getSupplierId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, supplier.getSupplierName());
                ps.setString(2, supplier.getContactPerson());
                ps.setString(3, supplier.getPhone());
                ps.setString(4, supplier.getEmail());
                ps.setString(5, supplier.getAddress());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            supplier.setSupplierId(rs.getInt(1));
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

    public boolean delete(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
