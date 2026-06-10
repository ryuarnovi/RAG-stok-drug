package com.pharmastock.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Medicine {

    public enum StockStatus {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK, EXPIRED, NEAR_EXPIRY
    }

    private int medicineId;
    private String medicineCode;
    private String medicineName;
    private String category;
    private String batchNumber;
    private String unit;
    private int stockQuantity;
    private int minimumStock;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private LocalDate expiryDate;
    private int supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient field for display purposes
    private String supplierName;

    public Medicine() {
        this.purchasePrice = BigDecimal.ZERO;
        this.sellingPrice = BigDecimal.ZERO;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public boolean isLowStock() {
        return stockQuantity > 0 && stockQuantity <= minimumStock;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isNearExpiry(int days) {
        if (expiryDate == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return !isExpired() && expiryDate.isBefore(threshold);
    }

    public StockStatus getStockStatus() {
        if (isExpired()) return StockStatus.EXPIRED;
        if (isNearExpiry(30)) return StockStatus.NEAR_EXPIRY;
        if (isOutOfStock()) return StockStatus.OUT_OF_STOCK;
        if (isLowStock()) return StockStatus.LOW_STOCK;
        return StockStatus.IN_STOCK;
    }

    public String getStockStatusLabel() {
        return switch (getStockStatus()) {
            case IN_STOCK -> "IN STOCK";
            case LOW_STOCK -> "LOW STOCK";
            case OUT_OF_STOCK -> "OUT OF STOCK";
            case EXPIRED -> "EXPIRED";
            case NEAR_EXPIRY -> "NEAR EXPIRY";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicine medicine = (Medicine) o;
        return medicineId == medicine.medicineId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicineId);
    }

    @Override
    public String toString() {
        return "Medicine{id=" + medicineId + ", code='" + medicineCode + "', name='" + medicineName + "', stock=" + stockQuantity + "}";
    }
}
