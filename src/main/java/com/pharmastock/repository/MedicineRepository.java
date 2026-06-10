package com.pharmastock.repository;

import com.pharmastock.config.DatabaseConfig;
import com.pharmastock.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicineRepository extends AbstractRepository<Medicine> implements IMedicineRepository {

    public MedicineRepository(DatabaseConfig db) {
        super(db);
    }

    public List<Medicine> findAll() {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id ORDER BY m.medicine_name";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error findAll medicines: " + e.getMessage());
        }
        return medicines;
    }

    public Optional<Medicine> findById(int medicineId) {
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.medicine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Medicine> findByCode(String code) {
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.medicine_code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findByCode: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Medicine> findByCategory(String category) {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.category = ? ORDER BY m.medicine_name";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findByCategory: " + e.getMessage());
        }
        return medicines;
    }

    public List<Medicine> searchByName(String query) {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.medicine_name LIKE ? OR m.medicine_code LIKE ? ORDER BY m.medicine_name";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searchByName: " + e.getMessage());
        }
        return medicines;
    }

    public List<Medicine> findLowStock() {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.stock_quantity > 0 AND m.stock_quantity <= m.minimum_stock ORDER BY m.stock_quantity ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error findLowStock: " + e.getMessage());
        }
        return medicines;
    }

    public List<Medicine> findExpired() {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.expiry_date IS NOT NULL AND m.expiry_date < CURDATE() ORDER BY m.expiry_date ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error findExpired: " + e.getMessage());
        }
        return medicines;
    }

    public List<Medicine> findNearExpiry(int days) {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE m.expiry_date IS NOT NULL AND m.expiry_date >= CURDATE() AND m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY m.expiry_date ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findNearExpiry: " + e.getMessage());
        }
        return medicines;
    }

    public List<Medicine> findAllPaginated(int page, int size, String sortBy, String sortDir, String category, String searchQuery, String stockFilter) {
        List<Medicine> medicines = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT m.*, s.supplier_name FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append(" AND m.category = ?");
            params.add(category);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql.append(" AND (m.medicine_name LIKE ? OR m.medicine_code LIKE ?)");
            String pattern = "%" + searchQuery + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (stockFilter != null && !stockFilter.isEmpty()) {
            switch (stockFilter) {
                case "LOW_STOCK" -> sql.append(" AND m.stock_quantity > 0 AND m.stock_quantity <= m.minimum_stock");
                case "EXPIRED" -> sql.append(" AND m.expiry_date IS NOT NULL AND m.expiry_date < CURDATE()");
                case "IN_STOCK" -> sql.append(" AND m.stock_quantity > m.minimum_stock");
                case "OUT_OF_STOCK" -> sql.append(" AND m.stock_quantity <= 0");
                case "NEAR_EXPIRY" -> sql.append(" AND m.expiry_date IS NOT NULL AND m.expiry_date >= CURDATE() AND m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)");
            }
        }

        String validSort = validateSortColumn(sortBy);
        String direction = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(validSort).append(" ").append(direction);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error findAllPaginated: " + e.getMessage());
        }
        return medicines;
    }

    public int getTotalCount(String category, String searchQuery, String stockFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM medicines m WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append(" AND m.category = ?");
            params.add(category);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql.append(" AND (m.medicine_name LIKE ? OR m.medicine_code LIKE ?)");
            String pattern = "%" + searchQuery + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (stockFilter != null && !stockFilter.isEmpty()) {
            switch (stockFilter) {
                case "LOW_STOCK" -> sql.append(" AND m.stock_quantity > 0 AND m.stock_quantity <= m.minimum_stock");
                case "EXPIRED" -> sql.append(" AND m.expiry_date IS NOT NULL AND m.expiry_date < CURDATE()");
                case "IN_STOCK" -> sql.append(" AND m.stock_quantity > m.minimum_stock");
                case "OUT_OF_STOCK" -> sql.append(" AND m.stock_quantity <= 0");
                case "NEAR_EXPIRY" -> sql.append(" AND m.expiry_date IS NOT NULL AND m.expiry_date >= CURDATE() AND m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)");
            }
        }

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getTotalCount: " + e.getMessage());
        }
        return 0;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM medicines";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error count: " + e.getMessage());
        }
        return 0;
    }

    public int sumStockQuantity() {
        String sql = "SELECT COALESCE(SUM(stock_quantity), 0) FROM medicines";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error sumStockQuantity: " + e.getMessage());
        }
        return 0;
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM medicines WHERE stock_quantity > 0 AND stock_quantity <= minimum_stock";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error countLowStock: " + e.getMessage());
        }
        return 0;
    }

    public int countExpired() {
        String sql = "SELECT COUNT(*) FROM medicines WHERE expiry_date IS NOT NULL AND expiry_date < CURDATE()";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error countExpired: " + e.getMessage());
        }
        return 0;
    }

    public int countNearExpiry(int days) {
        String sql = "SELECT COUNT(*) FROM medicines WHERE expiry_date IS NOT NULL AND expiry_date >= CURDATE() AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error countNearExpiry: " + e.getMessage());
        }
        return 0;
    }

    public List<String> findAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM medicines ORDER BY category";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error findAllCategories: " + e.getMessage());
        }
        return categories;
    }

    public int save(Medicine medicine) {
        String sql = "INSERT INTO medicines (medicine_code, medicine_name, category, batch_number, unit, stock_quantity, minimum_stock, purchase_price, selling_price, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicine.getMedicineCode());
            ps.setString(2, medicine.getMedicineName());
            ps.setString(3, medicine.getCategory());
            ps.setString(4, medicine.getBatchNumber());
            ps.setString(5, medicine.getUnit());
            ps.setInt(6, medicine.getStockQuantity());
            ps.setInt(7, medicine.getMinimumStock());
            ps.setBigDecimal(8, medicine.getPurchasePrice());
            ps.setBigDecimal(9, medicine.getSellingPrice());
            if (medicine.getExpiryDate() != null) {
                ps.setDate(10, Date.valueOf(medicine.getExpiryDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            if (medicine.getSupplierId() > 0) {
                ps.setInt(11, medicine.getSupplierId());
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error save medicine: " + e.getMessage());
        }
        return -1;
    }

    public boolean update(Medicine medicine) {
        String sql = "UPDATE medicines SET medicine_code = ?, medicine_name = ?, category = ?, batch_number = ?, unit = ?, stock_quantity = ?, minimum_stock = ?, purchase_price = ?, selling_price = ?, expiry_date = ?, supplier_id = ? WHERE medicine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicine.getMedicineCode());
            ps.setString(2, medicine.getMedicineName());
            ps.setString(3, medicine.getCategory());
            ps.setString(4, medicine.getBatchNumber());
            ps.setString(5, medicine.getUnit());
            ps.setInt(6, medicine.getStockQuantity());
            ps.setInt(7, medicine.getMinimumStock());
            ps.setBigDecimal(8, medicine.getPurchasePrice());
            ps.setBigDecimal(9, medicine.getSellingPrice());
            if (medicine.getExpiryDate() != null) {
                ps.setDate(10, Date.valueOf(medicine.getExpiryDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            if (medicine.getSupplierId() > 0) {
                ps.setInt(11, medicine.getSupplierId());
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            ps.setInt(12, medicine.getMedicineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error update medicine: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStock(int medicineId, int newQuantity) {
        String sql = "UPDATE medicines SET stock_quantity = ? WHERE medicine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updateStock: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error delete medicine: " + e.getMessage());
        }
        return false;
    }

    private String validateSortColumn(String column) {
        if (column == null) return "m.medicine_name";
        return switch (column) {
            case "medicine_code" -> "m.medicine_code";
            case "medicine_name" -> "m.medicine_name";
            case "category" -> "m.category";
            case "stock_quantity" -> "m.stock_quantity";
            case "expiry_date" -> "m.expiry_date";
            case "purchase_price" -> "m.purchase_price";
            case "selling_price" -> "m.selling_price";
            default -> "m.medicine_name";
        };
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setMedicineCode(rs.getString("medicine_code"));
        m.setMedicineName(rs.getString("medicine_name"));
        m.setCategory(rs.getString("category"));
        m.setBatchNumber(rs.getString("batch_number"));
        m.setUnit(rs.getString("unit"));
        m.setStockQuantity(rs.getInt("stock_quantity"));
        m.setMinimumStock(rs.getInt("minimum_stock"));
        m.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        m.setSellingPrice(rs.getBigDecimal("selling_price"));
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) m.setExpiryDate(expiryDate.toLocalDate());
        m.setSupplierId(rs.getInt("supplier_id"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) m.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) m.setUpdatedAt(updated.toLocalDateTime());
        try {
            m.setSupplierName(rs.getString("supplier_name"));
        } catch (SQLException ignored) {
            // supplier_name not in query
        }
        return m;
    }
}
