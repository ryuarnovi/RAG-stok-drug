package com.kepo.service;

import com.kepo.model.InventoryTransaction;
import com.kepo.model.Medicine;
import com.kepo.repository.InventoryTransactionRepository;
import com.kepo.repository.MedicineRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    private final MedicineRepository medicineRepo;
    private final InventoryTransactionRepository transactionRepo;
    private final UserService userService;

    public InventoryService(MedicineRepository medicineRepo, InventoryTransactionRepository transactionRepo, UserService userService) {
        this.medicineRepo = medicineRepo;
        this.transactionRepo = transactionRepo;
        this.userService = userService;
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepo.findAll();
    }

    public Medicine getMedicineById(int id) {
        return medicineRepo.findById(id);
    }

    public Medicine getMedicineByCode(String code) {
        return medicineRepo.findByCode(code);
    }

    public boolean saveMedicine(Medicine m) {
        boolean isNew = m.getMedicineId() <= 0;
        boolean res = medicineRepo.save(m);
        if (res && userService.getCurrentUser() != null) {
            String act = isNew ? "CREATE_MEDICINE" : "UPDATE_MEDICINE";
            userService.logActivity(userService.getCurrentUser().getUsername(), act, "Obat: " + m.getMedicineName() + " (Stok: " + m.getStockQuantity() + ")");
            
            // If it's a new medicine with stock, record initial stock transaction
            if (isNew && m.getStockQuantity() > 0) {
                InventoryTransaction t = new InventoryTransaction();
                t.setMedicineId(m.getMedicineId());
                t.setTransactionType("IN");
                t.setQuantity(m.getStockQuantity());
                t.setNotes("Stok awal registrasi obat.");
                transactionRepo.save(t);
            }
        }
        return res;
    }

    public boolean deleteMedicine(int id) {
        if (userService.getCurrentUser() != null) {
            userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_MEDICINE", "ID: " + id);
        }
        return medicineRepo.delete(id);
    }

    public boolean addStock(int medicineId, int qty, String notes) {
        if (qty <= 0) return false;
        boolean res = medicineRepo.updateStock(medicineId, qty);
        if (res) {
            InventoryTransaction t = new InventoryTransaction();
            t.setMedicineId(medicineId);
            t.setTransactionType("IN");
            t.setQuantity(qty);
            t.setNotes(notes);
            transactionRepo.save(t);
            
            if (userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "STOCK_IN", "Obat ID: " + medicineId + ", Qty: +" + qty);
            }
        }
        return res;
    }

    public boolean reduceStock(int medicineId, int qty, String notes) {
        if (qty <= 0) return false;
        boolean res = medicineRepo.updateStock(medicineId, -qty);
        if (res) {
            InventoryTransaction t = new InventoryTransaction();
            t.setMedicineId(medicineId);
            t.setTransactionType("OUT");
            t.setQuantity(qty);
            t.setNotes(notes);
            transactionRepo.save(t);

            if (userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "STOCK_OUT", "Obat ID: " + medicineId + ", Qty: -" + qty);
            }
        }
        return res;
    }

    public boolean adjustStock(int medicineId, int newQty, String notes) {
        Medicine m = medicineRepo.findById(medicineId);
        if (m == null) return false;
        int diff = newQty - m.getStockQuantity();
        if (diff == 0) return true;

        boolean res = medicineRepo.updateStock(medicineId, diff);
        if (res) {
            InventoryTransaction t = new InventoryTransaction();
            t.setMedicineId(medicineId);
            t.setTransactionType("ADJUSTMENT");
            t.setQuantity(diff);
            t.setNotes(notes);
            transactionRepo.save(t);

            if (userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "STOCK_ADJUST", "Obat ID: " + medicineId + ", Penyesuaian: " + diff);
            }
        }
        return res;
    }

    public List<Medicine> getLowStockMedicines() {
        List<Medicine> list = new ArrayList<>();
        for (Medicine m : medicineRepo.findAll()) {
            if (m.getStockQuantity() <= m.getMinimumStock()) {
                list.add(m);
            }
        }
        return list;
    }

    public List<Medicine> getNearExpiryMedicines(int days) {
        List<Medicine> list = new ArrayList<>();
        LocalDate limit = LocalDate.now().plusDays(days);
        for (Medicine m : medicineRepo.findAll()) {
            if (m.getExpiryDate() != null) {
                LocalDate exp = m.getExpiryDate().toLocalDate();
                if (!exp.isAfter(limit)) {
                    list.add(m);
                }
            }
        }
        return list;
    }

    public List<InventoryTransaction> getTransactionHistory() {
        return transactionRepo.findAll();
    }

    public List<InventoryTransaction> getTransactionHistory(int medicineId) {
        return transactionRepo.findByMedicineId(medicineId);
    }
}
