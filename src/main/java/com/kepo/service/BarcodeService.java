package com.kepo.service;

import com.kepo.model.Medicine;
import com.kepo.repository.MedicineRepository;
import com.kepo.util.BarcodeUtil;

import java.awt.image.BufferedImage;

public class BarcodeService {

    private final MedicineRepository medicineRepo;

    public BarcodeService(MedicineRepository medicineRepo) {
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

    public Medicine lookupMedicine(String barcode) {
        return medicineRepo.findByCode(barcode);
    }
}
