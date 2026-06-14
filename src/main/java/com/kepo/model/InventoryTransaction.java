package com.kepo.model;

import java.sql.Timestamp;

public class InventoryTransaction {
    private int transactionId;
    private int medicineId;
    private String transactionType; // IN, OUT, ADJUSTMENT
    private int quantity;
    private Timestamp transactionDate;
    private String notes;
    
    // UI auxiliary field
    private String medicineName;

    public InventoryTransaction() {}

    public InventoryTransaction(int transactionId, int medicineId, String transactionType, int quantity, Timestamp transactionDate, String notes) {
        this.transactionId = transactionId;
        this.medicineId = medicineId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
}
