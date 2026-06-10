package com.pharmastock.controller;

import com.pharmastock.model.Medicine;
import com.pharmastock.service.BarcodeService;
import com.pharmastock.service.InventoryService;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public class InventoryController {

    private final InventoryService inventoryService;
    private final BarcodeService barcodeService;

    private int currentPage = 0;
    private int pageSize = 20;
    private String currentSearch = "";
    private String currentCategory = "";
    private String currentStockFilter = "";
    private String sortColumn = "medicine_name";
    private boolean sortAscending = true;

    public InventoryController(InventoryService inventoryService, BarcodeService barcodeService) {
        this.inventoryService = inventoryService;
        this.barcodeService = barcodeService;
    }

    public List<Medicine> loadInventory() {
        return inventoryService.searchMedicines(
                currentSearch.isBlank() ? null : currentSearch,
                currentCategory.isBlank() ? null : currentCategory,
                currentStockFilter.isBlank() ? null : currentStockFilter,
                currentPage, pageSize);
    }

    public int getTotalPages() {
        int total = inventoryService.getTotalCount(
                currentCategory.isBlank() ? null : currentCategory,
                currentSearch.isBlank() ? null : currentSearch,
                currentStockFilter.isBlank() ? null : currentStockFilter);
        return Math.max(1, (int) Math.ceil((double) total / pageSize));
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPage(int page) {
        this.currentPage = Math.max(0, page);
    }

    public void nextPage() {
        if (currentPage < getTotalPages() - 1) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    public void searchMedicines(String query) {
        this.currentSearch = query != null ? query : "";
        this.currentPage = 0;
    }

    public void filterByCategory(String category) {
        this.currentCategory = category != null ? category : "";
        this.currentPage = 0;
    }

    public void filterByStatus(String status) {
        this.currentStockFilter = status != null ? status : "";
        this.currentPage = 0;
    }

    public void sortBy(String column) {
        if (column.equals(this.sortColumn)) {
            this.sortAscending = !this.sortAscending;
        } else {
            this.sortColumn = column;
            this.sortAscending = true;
        }
    }

    public int addMedicine(Medicine medicine) {
        return inventoryService.addMedicine(medicine);
    }

    public boolean updateMedicine(Medicine medicine) {
        return inventoryService.updateMedicine(medicine);
    }

    public boolean deleteMedicine(int id) {
        return inventoryService.deleteMedicine(id);
    }

    public Optional<Medicine> getMedicineById(int id) {
        return inventoryService.getMedicineById(id);
    }

    public Optional<Medicine> lookupByBarcode(String barcode) {
        return barcodeService.lookupMedicine(barcode);
    }

    public BufferedImage generateBarcode(String code) {
        return barcodeService.generateBarcode(code);
    }

    public List<String> getAllCategories() {
        return inventoryService.getAllCategories();
    }

    public boolean addStock(int medicineId, int quantity, String notes) {
        return inventoryService.addStock(medicineId, quantity, notes);
    }

    public boolean removeStock(int medicineId, int quantity, String notes) {
        return inventoryService.removeStock(medicineId, quantity, notes);
    }

    public String getCurrentSearch() { return currentSearch; }
    public String getCurrentCategory() { return currentCategory; }
    public String getCurrentStockFilter() { return currentStockFilter; }
}
