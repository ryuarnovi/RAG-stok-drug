package com.pharmastock.model;

public class AIRecommendation {

    public enum Type {
        REORDER, EXPIRY, INSIGHT, FORECAST
    }

    public enum Severity {
        INFO, WARNING, CRITICAL
    }

    private Type type;
    private String title;
    private String message;
    private Severity severity;
    private Medicine relatedMedicine;

    public AIRecommendation() {
    }

    public AIRecommendation(Type type, String title, String message, Severity severity) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
    }

    public AIRecommendation(Type type, String title, String message, Severity severity, Medicine relatedMedicine) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.relatedMedicine = relatedMedicine;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Medicine getRelatedMedicine() {
        return relatedMedicine;
    }

    public void setRelatedMedicine(Medicine relatedMedicine) {
        this.relatedMedicine = relatedMedicine;
    }

    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }

    public boolean isWarning() {
        return severity == Severity.WARNING;
    }

    @Override
    public String toString() {
        return "AIRecommendation{type=" + type + ", title='" + title + "', severity=" + severity + "}";
    }
}
