package com.kepo.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Medicine {
    private int medicineId;
    private String medicineCode;
    private String medicineName;
    private String category;
    private String batchNumber;
    private String unit;
    private int stockQuantity;
    private int minimumStock;
    private double purchasePrice;
    private double sellingPrice;
    private Date expiryDate;
    private Integer supplierId;
    private Timestamp createdAt;
    
    // UI auxiliary field
    private String supplierName;

    public Medicine() {}

    public Medicine(int medicineId, String medicineCode, String medicineName, String category, String batchNumber, String unit, int stockQuantity, int minimumStock, double purchasePrice, double sellingPrice, Date expiryDate, Integer supplierId, Timestamp createdAt) {
        this.medicineId = medicineId;
        this.medicineCode = medicineCode;
        this.medicineName = medicineName;
        this.category = category;
        this.batchNumber = batchNumber;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.minimumStock = minimumStock;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.expiryDate = expiryDate;
        this.supplierId = supplierId;
        this.createdAt = createdAt;
    }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineCode() { return medicineCode; }
    public void setMedicineCode(String medicineCode) { this.medicineCode = medicineCode; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}
