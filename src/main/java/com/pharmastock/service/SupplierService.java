package com.pharmastock.service;

import com.pharmastock.model.Supplier;
import com.pharmastock.repository.ISupplierRepository;
import com.pharmastock.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class SupplierService {

    private final ISupplierRepository supplierRepo;

    public SupplierService(ISupplierRepository supplierRepo) {
        this.supplierRepo = supplierRepo;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepo.findAll();
    }

    public Optional<Supplier> getSupplierById(int id) {
        return supplierRepo.findById(id);
    }

    public List<Supplier> searchSuppliers(String query) {
        if (query == null || query.isBlank()) {
            return supplierRepo.findAll();
        }
        return supplierRepo.searchByName(query);
    }

    public int addSupplier(Supplier supplier) throws IllegalArgumentException {
        validateSupplier(supplier);
        return supplierRepo.save(supplier);
    }

    public boolean updateSupplier(Supplier supplier) throws IllegalArgumentException {
        validateSupplier(supplier);
        return supplierRepo.update(supplier);
    }

    public boolean deleteSupplier(int id) {
        int medicineCount = supplierRepo.countMedicines(id);
        if (medicineCount > 0) {
            throw new IllegalStateException(
                    "Tidak dapat menghapus supplier yang masih memiliki " + medicineCount + " obat terdaftar.");
        }
        return supplierRepo.delete(id);
    }

    public int getMedicineCount(int supplierId) {
        return supplierRepo.countMedicines(supplierId);
    }

    private void validateSupplier(Supplier supplier) {
        if (!ValidationUtil.isNotEmpty(supplier.getSupplierName())) {
            throw new IllegalArgumentException("Nama supplier wajib diisi.");
        }
        if (supplier.getEmail() != null && !supplier.getEmail().isBlank()
                && !ValidationUtil.isValidEmail(supplier.getEmail())) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        if (supplier.getPhone() != null && !supplier.getPhone().isBlank()
                && !ValidationUtil.isValidPhone(supplier.getPhone())) {
            throw new IllegalArgumentException("Format nomor telepon tidak valid.");
        }
    }
}
