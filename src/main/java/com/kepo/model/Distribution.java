package com.kepo.model;

import java.sql.Timestamp;

public class Distribution {
    private int distributionId;
    private String docNum;
    private int shelterId;
    private String itemType; // OBAT, LOGISTIK, MAKANAN, PAKAIAN
    private int quantity;
    private String status; // DRAFT, APPROVED, SHIPPED, RECEIVED
    private String notes;
    private Timestamp createdAt;
    
    // UI auxiliary field
    private String shelterName;

    public Distribution() {}

    public Distribution(int distributionId, String docNum, int shelterId, String itemType, int quantity, String status, String notes, Timestamp createdAt) {
        this.distributionId = distributionId;
        this.docNum = docNum;
        this.shelterId = shelterId;
        this.itemType = itemType;
        this.quantity = quantity;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getDistributionId() { return distributionId; }
    public void setDistributionId(int distributionId) { this.distributionId = distributionId; }

    public String getDocNum() { return docNum; }
    public void setDocNum(String docNum) { this.docNum = docNum; }

    public int getShelterId() { return shelterId; }
    public void setShelterId(int shelterId) { this.shelterId = shelterId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
}
