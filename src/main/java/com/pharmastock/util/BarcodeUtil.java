package com.pharmastock.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

public final class BarcodeUtil {

    private BarcodeUtil() {
    }

    public static BufferedImage generateCode128(String content, int width, int height) {
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception e) {
            System.err.println("Error generating barcode: " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage generateQRCode(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    public static String decode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            System.err.println("Barcode tidak terdeteksi.");
            return null;
        }
    }
}
