package com.kepo.model;

import java.sql.Timestamp;

public class Refugee {
    private int refugeeId;
    private String name;
    private String nik;
    private int age;
    private String gender; // Laki-laki, Perempuan
    private String status; // CHECKED_IN, CHECKED_OUT
    private String medicalNotes;
    private Integer shelterId;
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private Timestamp createdAt;
    
    // UI auxiliary field
    private String shelterName;

    public Refugee() {}

    public Refugee(int refugeeId, String name, String nik, int age, String gender, String status, String medicalNotes, Integer shelterId, Timestamp checkInTime, Timestamp checkOutTime, Timestamp createdAt) {
        this.refugeeId = refugeeId;
        this.name = name;
        this.nik = nik;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.medicalNotes = medicalNotes;
        this.shelterId = shelterId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.createdAt = createdAt;
    }

    public int getRefugeeId() { return refugeeId; }
    public void setRefugeeId(int refugeeId) { this.refugeeId = refugeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public Integer getShelterId() { return shelterId; }
    public void setShelterId(Integer shelterId) { this.shelterId = shelterId; }

    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }

    public Timestamp getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Timestamp checkOutTime) { this.checkOutTime = checkOutTime; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
}
