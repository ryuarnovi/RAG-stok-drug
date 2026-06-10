package com.pharmastock.repository;

import com.pharmastock.config.DatabaseConfig;
import com.pharmastock.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierRepository extends AbstractRepository<Supplier> implements ISupplierRepository {

    public SupplierRepository(DatabaseConfig db) {
        super(db);
    }

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY supplier_name";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error findAll suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    public Optional<Supplier> findById(int supplierId) {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findById supplier: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Supplier> searchByName(String query) {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE supplier_name LIKE ? OR contact_person LIKE ? ORDER BY supplier_name";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searchByName suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    public int save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error save supplier: " + e.getMessage());
        }
        return -1;
    }

    public boolean update(Supplier supplier) {
        String sql = "UPDATE suppliers SET supplier_name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error update supplier: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error delete supplier: " + e.getMessage());
        }
        return false;
    }

    public int countMedicines(int supplierId) {
        String sql = "SELECT COUNT(*) FROM medicines WHERE supplier_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error countMedicines: " + e.getMessage());
        }
        return 0;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        return s;
    }
}
