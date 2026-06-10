package com.pharmastock.service;

import com.pharmastock.model.InventoryTransaction;
import com.pharmastock.model.Medicine;
import com.pharmastock.repository.IInventoryTransactionRepository;
import com.pharmastock.repository.IMedicineRepository;

import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final IMedicineRepository medicineRepo;
    private final IInventoryTransactionRepository transactionRepo;

    public InventoryService(IMedicineRepository medicineRepo, IInventoryTransactionRepository transactionRepo) {
        this.medicineRepo = medicineRepo;
        this.transactionRepo = transactionRepo;
    }

    // --- Medicine CRUD ---

    public List<Medicine> getAllMedicines() {
        return medicineRepo.findAll();
    }

    public Optional<Medicine> getMedicineById(int id) {
        return medicineRepo.findById(id);
    }

    public Optional<Medicine> getMedicineByCode(String code) {
        return medicineRepo.findByCode(code);
    }

    public int addMedicine(Medicine medicine) {
        int id = medicineRepo.save(medicine);
        if (id > 0 && medicine.getStockQuantity() > 0) {
            InventoryTransaction t = new InventoryTransaction(
                    id, "IN", medicine.getStockQuantity(), "Stok awal");
            transactionRepo.save(t);
        }
        return id;
    }

    public boolean updateMedicine(Medicine medicine) {
        return medicineRepo.update(medicine);
    }

    public boolean deleteMedicine(int id) {
        return medicineRepo.delete(id);
    }

    // --- Stock Operations ---

    public boolean addStock(int medicineId, int quantity, String notes) {
        Optional<Medicine> opt = medicineRepo.findById(medicineId);
        if (opt.isEmpty() || quantity <= 0) return false;

        Medicine medicine = opt.get();
        int newQty = medicine.getStockQuantity() + quantity;
        boolean updated = medicineRepo.updateStock(medicineId, newQty);

        if (updated) {
            InventoryTransaction t = new InventoryTransaction(medicineId, "IN", quantity, notes);
            transactionRepo.save(t);
        }
        return updated;
    }

    public boolean removeStock(int medicineId, int quantity, String notes) {
        Optional<Medicine> opt = medicineRepo.findById(medicineId);
        if (opt.isEmpty() || quantity <= 0) return false;

        Medicine medicine = opt.get();
        if (medicine.getStockQuantity() < quantity) {
            return false; // stok tidak cukup
        }

        int newQty = medicine.getStockQuantity() - quantity;
        boolean updated = medicineRepo.updateStock(medicineId, newQty);

        if (updated) {
            InventoryTransaction t = new InventoryTransaction(medicineId, "OUT", quantity, notes);
            transactionRepo.save(t);
        }
        return updated;
    }

    public boolean adjustStock(int medicineId, int newQuantity, String notes) {
        Optional<Medicine> opt = medicineRepo.findById(medicineId);
        if (opt.isEmpty() || newQuantity < 0) return false;

        Medicine medicine = opt.get();
        int diff = newQuantity - medicine.getStockQuantity();
        boolean updated = medicineRepo.updateStock(medicineId, newQuantity);

        if (updated) {
            InventoryTransaction t = new InventoryTransaction(medicineId, "ADJUSTMENT", diff, notes);
            transactionRepo.save(t);
        }
        return updated;
    }

    // --- Search / Filter / Pagination ---

    public List<Medicine> searchMedicines(String query, String category, String stockFilter, int page, int size) {
        return medicineRepo.findAllPaginated(page, size, "medicine_name", "ASC", category, query, stockFilter);
    }

    public int getTotalCount(String category, String searchQuery, String stockFilter) {
        return medicineRepo.getTotalCount(category, searchQuery, stockFilter);
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepo.findLowStock();
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepo.findExpired();
    }

    public List<Medicine> getNearExpiryMedicines(int days) {
        return medicineRepo.findNearExpiry(days);
    }

    public List<String> getAllCategories() {
        return medicineRepo.findAllCategories();
    }

    // --- Dashboard Stats ---

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.totalSKU = medicineRepo.count();
        stats.totalInventory = medicineRepo.sumStockQuantity();
        stats.lowStockCount = medicineRepo.countLowStock();
        stats.expiredCount = medicineRepo.countExpired();
        stats.nearExpiredCount = medicineRepo.countNearExpiry(30);
        // Growth calculation: simplified as percentage
        stats.monthlyGrowth = 12.0; // Placeholder - could compare current vs last month counts
        return stats;
    }

    // --- Activity ---

    public List<InventoryTransaction> getRecentActivity(int limit) {
        return transactionRepo.findRecent(limit);
    }

    public List<InventoryTransaction> getTransactionsByMedicine(int medicineId) {
        return transactionRepo.findByMedicineId(medicineId);
    }

    // --- Dashboard Stats DTO ---

    public static class DashboardStats {
        public int totalSKU;
        public int totalInventory;
        public int lowStockCount;
        public int expiredCount;
        public int nearExpiredCount;
        public double monthlyGrowth;
    }
}
