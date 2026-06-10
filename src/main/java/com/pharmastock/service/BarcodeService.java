package com.pharmastock.service;

import com.pharmastock.model.Medicine;
import com.pharmastock.repository.IMedicineRepository;
import com.pharmastock.util.BarcodeUtil;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class BarcodeService {

    private final IMedicineRepository medicineRepo;

    public BarcodeService(IMedicineRepository medicineRepo) {
        this.medicineRepo = medicineRepo;
    }

    public BufferedImage generateBarcode(String code) {
        return BarcodeUtil.generateCode128(code, 300, 80);
    }

    public BufferedImage generateQRCode(String data) {
        return BarcodeUtil.generateQRCode(data, 200, 200);
    }

    public String decodeBarcode(BufferedImage image) {
        return BarcodeUtil.decode(image);
    }

    public Optional<Medicine> lookupMedicine(String barcode) {
        return medicineRepo.findByCode(barcode);
    }
}
