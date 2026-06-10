package com.pharmastock.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class InventoryTransaction {

    public enum TransactionType {
        IN, OUT, ADJUSTMENT
    }

    private int transactionId;
    private int medicineId;
    private String transactionType;
    private int quantity;
    private LocalDateTime transactionDate;
    private String notes;

    // Transient
    private String medicineName;
    private String medicineCode;

    public InventoryTransaction() {
    }

    public InventoryTransaction(int medicineId, String transactionType, int quantity, String notes) {
        this.medicineId = medicineId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.notes = notes;
        this.transactionDate = LocalDateTime.now();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public boolean isIncoming() {
        return "IN".equals(transactionType);
    }

    public boolean isOutgoing() {
        return "OUT".equals(transactionType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryTransaction that = (InventoryTransaction) o;
        return transactionId == that.transactionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", type=" + transactionType + ", qty=" + quantity + "}";
    }
}
