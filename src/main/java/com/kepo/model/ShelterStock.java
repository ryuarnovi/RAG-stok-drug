package com.kepo.model;

import java.sql.Timestamp;

public class ShelterStock {
    private int shelterStockId;
    private int shelterId;
    private int medicineId;
    private int quantity;
    private int minimumStock;
    private Timestamp updatedAt;

    // Join fields for UI
    private String medicineName;
    private String medicineCode;
    private String category;
    private String unit;

    public ShelterStock() {}

    public ShelterStock(int shelterStockId, int shelterId, int medicineId, int quantity, int minimumStock, Timestamp updatedAt) {
        this.shelterStockId = shelterStockId;
        this.shelterId = shelterId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
        this.updatedAt = updatedAt;
    }

    public int getShelterStockId() { return shelterStockId; }
    public void setShelterStockId(int shelterStockId) { this.shelterStockId = shelterStockId; }

    public int getShelterId() { return shelterId; }
    public void setShelterId(int shelterId) { this.shelterId = shelterId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getMedicineCode() { return medicineCode; }
    public void setMedicineCode(String medicineCode) { this.medicineCode = medicineCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
