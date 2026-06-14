package com.kepo.model;

import java.sql.Timestamp;

public class Donor {
    private int donorId;
    private String donorName;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private Timestamp createdAt;

    public Donor() {}

    public Donor(int donorId, String donorName, String contact, String phone, String email, String address, Timestamp createdAt) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.contact = contact;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
    }

    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return donorName;
    }
}
