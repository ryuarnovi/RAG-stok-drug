package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Donor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorRepository {

    private final DatabaseConfig dbConfig;

    public DonorRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Donor> findAll() {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT donor_id, donor_name, contact, phone, email, address, created_at FROM donors ORDER BY donor_name ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDonor(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Donor findById(int donorId) {
        String sql = "SELECT donor_id, donor_name, contact, phone, email, address, created_at FROM donors WHERE donor_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDonor(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Donor donor) {
        if (donor.getDonorId() > 0) {
            String sql = "UPDATE donors SET donor_name = ?, contact = ?, phone = ?, email = ?, address = ? WHERE donor_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, donor.getDonorName());
                ps.setString(2, donor.getContact());
                ps.setString(3, donor.getPhone());
                ps.setString(4, donor.getEmail());
                ps.setString(5, donor.getAddress());
                ps.setInt(6, donor.getDonorId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO donors (donor_name, contact, phone, email, address) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, donor.getDonorName());
                ps.setString(2, donor.getContact());
                ps.setString(3, donor.getPhone());
                ps.setString(4, donor.getEmail());
                ps.setString(5, donor.getAddress());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            donor.setDonorId(rs.getInt(1));
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

    public boolean delete(int donorId) {
        String sql = "DELETE FROM donors WHERE donor_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, donorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Donor mapResultSetToDonor(ResultSet rs) throws SQLException {
        Donor d = new Donor();
        d.setDonorId(rs.getInt("donor_id"));
        d.setDonorName(rs.getString("donor_name"));
        d.setContact(rs.getString("contact"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setAddress(rs.getString("address"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        return d;
    }
}
