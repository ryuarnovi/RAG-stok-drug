package com.pharmastock.controller;

import com.pharmastock.model.Supplier;
import com.pharmastock.service.SupplierService;

import java.util.List;
import java.util.Optional;

public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    public List<Supplier> searchSuppliers(String query) {
        return supplierService.searchSuppliers(query);
    }

    public Optional<Supplier> getSupplierById(int id) {
        return supplierService.getSupplierById(id);
    }

    public int addSupplier(Supplier supplier) throws IllegalArgumentException {
        return supplierService.addSupplier(supplier);
    }

    public boolean updateSupplier(Supplier supplier) throws IllegalArgumentException {
        return supplierService.updateSupplier(supplier);
    }

    public boolean deleteSupplier(int id) throws IllegalStateException {
        return supplierService.deleteSupplier(id);
    }

    public int getMedicineCount(int supplierId) {
        return supplierService.getMedicineCount(supplierId);
    }
}
