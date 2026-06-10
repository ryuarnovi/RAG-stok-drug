package com.pharmastock.repository;

import com.pharmastock.model.InventoryTransaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IInventoryTransactionRepository extends BaseRepository<InventoryTransaction> {
    List<InventoryTransaction> findRecent(int limit);
    List<InventoryTransaction> findByMedicineId(int medicineId);
    List<InventoryTransaction> findByDateRange(LocalDateTime from, LocalDateTime to);
    Map<String, Integer> getMonthlyOutMovement(int medicineId, int months);
    int countOutTransactions(int medicineId, int days);
    int sumOutQuantity(int medicineId, int days);
}
