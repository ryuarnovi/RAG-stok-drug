package com.kepo.model;

import java.sql.Timestamp;

public class Shelter {
    private int shelterId;
    private String name;
    private String location;
    private int capacity;
    private int currentOccupancy;
    private String status; // AMAN, WASPADA, KRITIS
    private String penanggungJawab;
    private Integer eventId;
    private Timestamp createdAt;

    public Shelter() {}

    public Shelter(int shelterId, String name, String location, int capacity, int currentOccupancy, String status, String penanggungJawab, Integer eventId, Timestamp createdAt) {
        this.shelterId = shelterId;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.currentOccupancy = currentOccupancy;
        this.status = status;
        this.penanggungJawab = penanggungJawab;
        this.eventId = eventId;
        this.createdAt = createdAt;
    }

    public int getShelterId() { return shelterId; }
    public void setShelterId(int shelterId) { this.shelterId = shelterId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPenanggungJawab() { return penanggungJawab; }
    public void setPenanggungJawab(String penanggungJawab) { this.penanggungJawab = penanggungJawab; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return name;
    }
}
