package com.kepo.model;

import java.sql.Timestamp;

public class MedicineRequest {
    private int requestId;
    private int refugeeId;
    private int shelterId;
    private String medicineCode;
    private String medicineName;
    private int quantity;
    private String status; // PENDING, APPROVED, REJECTED, FULFILLED
    private String notes;
    private Timestamp createdAt;

    private String refugeeName;
    private String shelterName;

    public MedicineRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getRefugeeId() { return refugeeId; }
    public void setRefugeeId(int refugeeId) { this.refugeeId = refugeeId; }
    public int getShelterId() { return shelterId; }
    public void setShelterId(int shelterId) { this.shelterId = shelterId; }
    public String getMedicineCode() { return medicineCode; }
    public void setMedicineCode(String medicineCode) { this.medicineCode = medicineCode; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getRefugeeName() { return refugeeName; }
    public void setRefugeeName(String refugeeName) { this.refugeeName = refugeeName; }
    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
}
