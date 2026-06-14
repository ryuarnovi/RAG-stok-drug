package com.kepo.controller;

import com.kepo.model.Medicine;
import com.kepo.service.BarcodeService;
import com.kepo.service.InventoryService;

import java.awt.image.BufferedImage;
import java.util.List;

public class InventoryController {

    private final InventoryService inventoryService;
    private final BarcodeService barcodeService;

    public InventoryController(InventoryService inventoryService, BarcodeService barcodeService) {
        this.inventoryService = inventoryService;
        this.barcodeService = barcodeService;
    }

    public List<Medicine> getAllMedicines() {
        return inventoryService.getAllMedicines();
    }

    public Medicine getMedicineById(int id) {
        return inventoryService.getMedicineById(id);
    }

    public Medicine getMedicineByCode(String code) {
        return inventoryService.getMedicineByCode(code);
    }

    public boolean saveMedicine(Medicine m) {
        return inventoryService.saveMedicine(m);
    }

    public boolean deleteMedicine(int id) {
        return inventoryService.deleteMedicine(id);
    }

    public boolean addStock(int medicineId, int quantity, String notes) {
        return inventoryService.addStock(medicineId, quantity, notes);
    }

    public boolean reduceStock(int medicineId, int quantity, String notes) {
        return inventoryService.reduceStock(medicineId, quantity, notes);
    }

    public boolean adjustStock(int medicineId, int quantity, String notes) {
        return inventoryService.adjustStock(medicineId, quantity, notes);
    }

    public Medicine lookupByBarcode(String barcode) {
        return barcodeService.lookupMedicine(barcode);
    }

    public BufferedImage generateBarcode(String code) {
        return barcodeService.generateBarcode(code);
    }
}
