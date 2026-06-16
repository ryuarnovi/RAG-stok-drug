package com.kepo.model;

import java.sql.Timestamp;

public class RefugeeMovement {
    private int movementId;
    private int refugeeId;
    private Integer fromShelterId;
    private Integer toShelterId;
    private String movedBy;
    private String notes;
    private Timestamp movedAt;

    // Join fields for UI
    private String refugeeName;
    private String fromShelterName;
    private String toShelterName;

    public RefugeeMovement() {}

    public RefugeeMovement(int movementId, int refugeeId, Integer fromShelterId, Integer toShelterId, String movedBy, String notes, Timestamp movedAt) {
        this.movementId = movementId;
        this.refugeeId = refugeeId;
        this.fromShelterId = fromShelterId;
        this.toShelterId = toShelterId;
        this.movedBy = movedBy;
        this.notes = notes;
        this.movedAt = movedAt;
    }

    public int getMovementId() { return movementId; }
    public void setMovementId(int movementId) { this.movementId = movementId; }

    public int getRefugeeId() { return refugeeId; }
    public void setRefugeeId(int refugeeId) { this.refugeeId = refugeeId; }

    public Integer getFromShelterId() { return fromShelterId; }
    public void setFromShelterId(Integer fromShelterId) { this.fromShelterId = fromShelterId; }

    public Integer getToShelterId() { return toShelterId; }
    public void setToShelterId(Integer toShelterId) { this.toShelterId = toShelterId; }

    public String getMovedBy() { return movedBy; }
    public void setMovedBy(String movedBy) { this.movedBy = movedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getMovedAt() { return movedAt; }
    public void setMovedAt(Timestamp movedAt) { this.movedAt = movedAt; }

    public String getRefugeeName() { return refugeeName; }
    public void setRefugeeName(String refugeeName) { this.refugeeName = refugeeName; }

    public String getFromShelterName() { return fromShelterName; }
    public void setFromShelterName(String fromShelterName) { this.fromShelterName = fromShelterName; }

    public String getToShelterName() { return toShelterName; }
    public void setToShelterName(String toShelterName) { this.toShelterName = toShelterName; }
}
