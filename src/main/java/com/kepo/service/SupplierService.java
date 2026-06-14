package com.kepo.service;

import com.kepo.model.Supplier;
import com.kepo.repository.SupplierRepository;

import java.util.List;

public class SupplierService {

    private final SupplierRepository supplierRepo;
    private final UserService userService;

    public SupplierService(SupplierRepository supplierRepo, UserService userService) {
        this.supplierRepo = supplierRepo;
        this.userService = userService;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepo.findAll();
    }

    public Supplier getSupplierById(int id) {
        return supplierRepo.findById(id);
    }

    public boolean saveSupplier(Supplier s) {
        boolean res = supplierRepo.save(s);
        if (res && userService.getCurrentUser() != null) {
            String act = s.getSupplierId() > 0 ? "UPDATE_SUPPLIER" : "CREATE_SUPPLIER";
            userService.logActivity(userService.getCurrentUser().getUsername(), act, "Supplier: " + s.getSupplierName());
        }
        return res;
    }

    public boolean deleteSupplier(int id) {
        if (userService.getCurrentUser() != null) {
            userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_SUPPLIER", "ID: " + id);
        }
        return supplierRepo.delete(id);
    }
}
