package com.pharmastock.model;

import java.time.LocalDateTime;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String type; // "WARNING", "DANGER", "INFO"
    private boolean read;
    private LocalDateTime timestamp;

    public Notification(String id, String title, String message, String type, LocalDateTime timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
