package com.kepo.model;

import java.sql.Timestamp;

public class Event {
    private int eventId;
    private String name;
    private String location;
    private String status; // ACTIVE, CLOSED
    private String description;
    private int shelterCount;
    private Timestamp createdAt;

    public Event() {}

    public Event(int eventId, String name, String location, String status, String description, int shelterCount, Timestamp createdAt) {
        this.eventId = eventId;
        this.name = name;
        this.location = location;
        this.status = status;
        this.description = description;
        this.shelterCount = shelterCount;
        this.createdAt = createdAt;
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getShelterCount() { return shelterCount; }
    public void setShelterCount(int shelterCount) { this.shelterCount = shelterCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}
