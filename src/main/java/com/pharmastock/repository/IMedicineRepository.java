package com.pharmastock.repository;

import com.pharmastock.model.Medicine;
import java.util.List;
import java.util.Optional;

public interface IMedicineRepository extends BaseRepository<Medicine> {
    Optional<Medicine> findByCode(String code);
    List<Medicine> findByCategory(String category);
    List<Medicine> searchByName(String query);
    List<Medicine> findLowStock();
    List<Medicine> findExpired();
    List<Medicine> findNearExpiry(int days);
    List<Medicine> findAllPaginated(int page, int size, String sortBy, String sortDir, String category, String searchQuery, String stockFilter);
    int getTotalCount(String category, String searchQuery, String stockFilter);
    int count();
    int sumStockQuantity();
    int countLowStock();
    int countExpired();
    int countNearExpiry(int days);
    List<String> findAllCategories();
    boolean updateStock(int medicineId, int newQuantity);
}
