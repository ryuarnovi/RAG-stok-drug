package com.kepo.model;

import java.sql.Timestamp;

public class Supplier {
    private int supplierId;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Timestamp createdAt;

    public Supplier() {}

    public Supplier(int supplierId, String supplierName, String contactPerson, String phone, String email, String address, Timestamp createdAt) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
    }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

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
        return supplierName;
    }
}
