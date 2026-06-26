package com.kepo.service;

import com.kepo.model.*;
import com.kepo.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    private final ShelterRepository shelterRepo;
    private final RefugeeRepository refugeeRepo;
    private final MedicineRepository medicineRepo;
    private final DistributionRepository distributionRepo;
    private final DonorRepository donorRepo;
    private final EventRepository eventRepo;
    private final String outputDir;

    public ReportService(ShelterRepository shelterRepo, RefugeeRepository refugeeRepo,
                         MedicineRepository medicineRepo, DistributionRepository distributionRepo,
                         DonorRepository donorRepo, EventRepository eventRepo, String outputDir) {
        this.shelterRepo = shelterRepo;
        this.refugeeRepo = refugeeRepo;
        this.medicineRepo = medicineRepo;
        this.distributionRepo = distributionRepo;
        this.donorRepo = donorRepo;
        this.eventRepo = eventRepo;
        this.outputDir = outputDir;

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String generateShelterReport(String format) throws Exception {
        List<Shelter> data = shelterRepo.findAll();
        String baseName = "laporan_shelter";
        if ("EXCEL".equalsIgnoreCase(format)) return generateShelterExcel(data, baseName);
        if ("CSV".equalsIgnoreCase(format)) return generateShelterCsv(data, baseName);
        return generateShelterPdf(data, baseName);
    }

    public String generateRefugeeReport(String format) throws Exception {
        List<Refugee> data = refugeeRepo.findAll();
        String baseName = "laporan_pengungsi";
        if ("EXCEL".equalsIgnoreCase(format)) return generateRefugeeExcel(data, baseName);
        if ("CSV".equalsIgnoreCase(format)) return generateRefugeeCsv(data, baseName);
        return generateRefugeePdf(data, baseName);
    }

    public String generateInventoryReport(String format) throws Exception {
        List<Medicine> data = medicineRepo.findAll();
        String baseName = "laporan_inventaris_obat";
        if ("EXCEL".equalsIgnoreCase(format)) return generateInventoryExcel(data, baseName);
        if ("CSV".equalsIgnoreCase(format)) return generateInventoryCsv(data, baseName);
        return generateInventoryPdf(data, baseName);
    }

    public String generateDistributionReport(String format) throws Exception {
        List<Distribution> data = distributionRepo.findAll();
        String baseName = "laporan_distribusi_bantuan";
        if ("EXCEL".equalsIgnoreCase(format)) return generateDistributionExcel(data, baseName);
        if ("CSV".equalsIgnoreCase(format)) return generateDistributionCsv(data, baseName);
        return generateDistributionPdf(data, baseName);
    }

    public String generateDonorReport(String format) throws Exception {
        List<Donor> data = donorRepo.findAll();
        String baseName = "laporan_donatur";
        if ("EXCEL".equalsIgnoreCase(format)) return generateDonorExcel(data, baseName);
        if ("CSV".equalsIgnoreCase(format)) return generateDonorCsv(data, baseName);
        return generateDonorPdf(data, baseName);
    }

    public String generateEventReport(String format) throws Exception {
        List<Event> data = eventRepo.findAll();
        String baseName = "laporan_event_bencana";
        if ("EXCEL".equalsIgnoreCase(format)) return generateEventExcel(data, baseName);
        if ("CSV".equalsIgnoreCase(format)) return generateEventCsv(data, baseName);
        return generateEventPdf(data, baseName);
    }

    // ============ CSV GENERATORS ============

    private String generateShelterCsv(List<Shelter> list, String baseName) throws Exception {
        return generateCsv(baseName, new String[]{"ID", "Nama Shelter", "Lokasi", "Kapasitas", "Terisi", "Status", "Penanggung Jawab"},
            list, s -> new String[]{String.valueOf(s.getShelterId()), s.getName(), s.getLocation(),
                String.valueOf(s.getCapacity()), String.valueOf(s.getCurrentOccupancy()), s.getStatus(), s.getPenanggungJawab()});
    }

    private String generateRefugeeCsv(List<Refugee> list, String baseName) throws Exception {
        return generateCsv(baseName, new String[]{"ID", "Nama", "NIK", "Usia", "Gender", "Status", "Shelter", "Catatan Medis"},
            list, r -> new String[]{String.valueOf(r.getRefugeeId()), r.getName(), r.getNik(),
                String.valueOf(r.getAge()), r.getGender(), r.getStatus(),
                r.getShelterName() != null ? r.getShelterName() : "N/A",
                r.getMedicalNotes() != null ? r.getMedicalNotes() : "-"});
    }

    private String generateInventoryCsv(List<Medicine> list, String baseName) throws Exception {
        return generateCsv(baseName, new String[]{"Kode", "Nama Obat", "Kategori", "Stok", "Min Stok", "Unit", "Exp Date", "Supplier"},
            list, m -> new String[]{m.getMedicineCode(), m.getMedicineName(), m.getCategory(),
                String.valueOf(m.getStockQuantity()), String.valueOf(m.getMinimumStock()), m.getUnit(),
                m.getExpiryDate() != null ? m.getExpiryDate().toString() : "-",
                m.getSupplierName() != null ? m.getSupplierName() : "N/A"});
    }

    private String generateDistributionCsv(List<Distribution> list, String baseName) throws Exception {
        return generateCsv(baseName, new String[]{"No Dokumen", "Shelter Tujuan", "Tipe", "Jumlah", "Status", "Keterangan"},
            list, d -> new String[]{d.getDocNum(), d.getShelterName() != null ? d.getShelterName() : "N/A",
                d.getItemType(), String.valueOf(d.getQuantity()), d.getStatus(),
                d.getNotes() != null ? d.getNotes() : "-"});
    }

    private String generateDonorCsv(List<Donor> list, String baseName) throws Exception {
        return generateCsv(baseName, new String[]{"ID", "Nama Donatur", "Kontak", "Telepon", "Email", "Alamat"},
            list, d -> new String[]{String.valueOf(d.getDonorId()), d.getDonorName(),
                d.getContact() != null ? d.getContact() : "-", d.getPhone() != null ? d.getPhone() : "-",
                d.getEmail() != null ? d.getEmail() : "-", d.getAddress() != null ? d.getAddress() : "-"});
    }

    private String generateEventCsv(List<Event> list, String baseName) throws Exception {
        return generateCsv(baseName, new String[]{"ID", "Nama Event", "Lokasi", "Status", "Deskripsi"},
            list, e -> new String[]{String.valueOf(e.getEventId()), e.getName(), e.getLocation(),
                e.getStatus(), e.getDescription() != null ? e.getDescription() : "-"});
    }

    private <T> String generateCsv(String baseName, String[] headers, List<T> items, java.util.function.Function<T, String[]> mapper) throws Exception {
        String filePath = getFilePath(baseName, ".csv");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(filePath, "UTF-8")) {
            pw.println(String.join(",", headers));
            for (T item : items) {
                String[] vals = mapper.apply(item);
                for (int i = 0; i < vals.length; i++) {
                    if (vals[i].contains(",") || vals[i].contains("\"") || vals[i].contains("\n")) {
                        vals[i] = "\"" + vals[i].replace("\"", "\"\"") + "\"";
                    }
                }
                pw.println(String.join(",", vals));
            }
        }
        return filePath;
    }

    // ============ EXCEL GENERATORS ============

    private String generateShelterExcel(List<Shelter> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Shelter");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama Shelter", "Lokasi", "Kapasitas", "Terisi", "Status", "Penanggung Jawab"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowIdx = 1;
            for (Shelter s : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(s.getShelterId());
                r.createCell(1).setCellValue(s.getName());
                r.createCell(2).setCellValue(s.getLocation());
                r.createCell(3).setCellValue(s.getCapacity());
                r.createCell(4).setCellValue(s.getCurrentOccupancy());
                r.createCell(5).setCellValue(s.getStatus());
                r.createCell(6).setCellValue(s.getPenanggungJawab());
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
        return filePath;
    }

    private String generateRefugeeExcel(List<Refugee> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Pengungsi");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama", "NIK", "Usia", "Gender", "Status", "Shelter", "Catatan Medis", "Waktu Masuk", "Waktu Keluar"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowIdx = 1;
            for (Refugee ref : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(ref.getRefugeeId());
                r.createCell(1).setCellValue(ref.getName());
                r.createCell(2).setCellValue(ref.getNik());
                r.createCell(3).setCellValue(ref.getAge());
                r.createCell(4).setCellValue(ref.getGender());
                r.createCell(5).setCellValue(ref.getStatus());
                r.createCell(6).setCellValue(ref.getShelterName() != null ? ref.getShelterName() : "N/A");
                r.createCell(7).setCellValue(ref.getMedicalNotes() != null ? ref.getMedicalNotes() : "-");
                r.createCell(8).setCellValue(ref.getCheckInTime() != null ? ref.getCheckInTime().toString() : "-");
                r.createCell(9).setCellValue(ref.getCheckOutTime() != null ? ref.getCheckOutTime().toString() : "-");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
        return filePath;
    }

    private String generateInventoryExcel(List<Medicine> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inventaris Obat");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Kode Obat", "Nama Obat", "Kategori", "Batch", "Unit", "Stok", "Min Stok", "Harga Beli", "Harga Jual", "Expiry Date", "Supplier"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowIdx = 1;
            for (Medicine m : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(m.getMedicineId());
                r.createCell(1).setCellValue(m.getMedicineCode());
                r.createCell(2).setCellValue(m.getMedicineName());
                r.createCell(3).setCellValue(m.getCategory());
                r.createCell(4).setCellValue(m.getBatchNumber() != null ? m.getBatchNumber() : "-");
                r.createCell(5).setCellValue(m.getUnit());
                r.createCell(6).setCellValue(m.getStockQuantity());
                r.createCell(7).setCellValue(m.getMinimumStock());
                r.createCell(8).setCellValue(m.getPurchasePrice());
                r.createCell(9).setCellValue(m.getSellingPrice());
                r.createCell(10).setCellValue(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "-");
                r.createCell(11).setCellValue(m.getSupplierName() != null ? m.getSupplierName() : "N/A");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
        return filePath;
    }

    private String generateDistributionExcel(List<Distribution> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Distribusi");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "No Dokumen", "Shelter Tujuan", "Tipe Bantuan", "Jumlah", "Status", "Keterangan"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowIdx = 1;
            for (Distribution d : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(d.getDistributionId());
                r.createCell(1).setCellValue(d.getDocNum());
                r.createCell(2).setCellValue(d.getShelterName() != null ? d.getShelterName() : "N/A");
                r.createCell(3).setCellValue(d.getItemType());
                r.createCell(4).setCellValue(d.getQuantity());
                r.createCell(5).setCellValue(d.getStatus());
                r.createCell(6).setCellValue(d.getNotes() != null ? d.getNotes() : "-");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
        return filePath;
    }

    private String generateDonorExcel(List<Donor> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Donatur");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama Donatur", "Kontak", "Telepon", "Email", "Alamat"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowIdx = 1;
            for (Donor d : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(d.getDonorId());
                r.createCell(1).setCellValue(d.getDonorName());
                r.createCell(2).setCellValue(d.getContact() != null ? d.getContact() : "-");
                r.createCell(3).setCellValue(d.getPhone() != null ? d.getPhone() : "-");
                r.createCell(4).setCellValue(d.getEmail() != null ? d.getEmail() : "-");
                r.createCell(5).setCellValue(d.getAddress() != null ? d.getAddress() : "-");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
        return filePath;
    }

    private String generateEventExcel(List<Event> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Event Bencana");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama Event", "Lokasi", "Status", "Deskripsi", "Waktu Mulai"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowIdx = 1;
            for (Event e : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(e.getEventId());
                r.createCell(1).setCellValue(e.getName());
                r.createCell(2).setCellValue(e.getLocation());
                r.createCell(3).setCellValue(e.getStatus());
                r.createCell(4).setCellValue(e.getDescription() != null ? e.getDescription() : "-");
                r.createCell(5).setCellValue(e.getCreatedAt() != null ? e.getCreatedAt().toString() : "-");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
        return filePath;
    }

    // ============ PDF GENERATORS ============

    private String generateShelterPdf(List<Shelter> list, String baseName) throws Exception {
        return generatePdf(baseName, "Laporan Pemantauan Shelter",
            new String[]{"ID", "Nama Shelter", "Lokasi", "Kapasitas", "Terisi", "Status", "PJ"},
            list, s -> new String[]{String.valueOf(s.getShelterId()), s.getName(), s.getLocation(),
                String.valueOf(s.getCapacity()), String.valueOf(s.getCurrentOccupancy()), s.getStatus(), s.getPenanggungJawab()});
    }

    private String generateRefugeePdf(List<Refugee> list, String baseName) throws Exception {
        return generatePdf(baseName, "Laporan Registrasi Pengungsi",
            new String[]{"Nama", "NIK", "Usia", "Gender", "Status", "Shelter", "Medis"},
            list, r -> new String[]{r.getName(), r.getNik(), String.valueOf(r.getAge()), r.getGender(),
                r.getStatus(), r.getShelterName() != null ? r.getShelterName() : "N/A",
                r.getMedicalNotes() != null ? r.getMedicalNotes().length() > 20 ? r.getMedicalNotes().substring(0, 20) + ".." : r.getMedicalNotes() : "-"});
    }

    private String generateInventoryPdf(List<Medicine> list, String baseName) throws Exception {
        return generatePdf(baseName, "Laporan Inventaris Obat",
            new String[]{"Kode", "Nama Obat", "Kategori", "Stok", "Min", "Unit", "Exp Date"},
            list, m -> new String[]{m.getMedicineCode(), m.getMedicineName(), m.getCategory(),
                String.valueOf(m.getStockQuantity()), String.valueOf(m.getMinimumStock()), m.getUnit(),
                m.getExpiryDate() != null ? m.getExpiryDate().toString() : "-"});
    }

    private String generateDistributionPdf(List<Distribution> list, String baseName) throws Exception {
        return generatePdf(baseName, "Laporan Distribusi Bantuan",
            new String[]{"No Dokumen", "Shelter", "Tipe", "Jumlah", "Status"},
            list, d -> new String[]{d.getDocNum(), d.getShelterName() != null ? d.getShelterName() : "N/A",
                d.getItemType(), String.valueOf(d.getQuantity()), d.getStatus()});
    }

    private String generateDonorPdf(List<Donor> list, String baseName) throws Exception {
        return generatePdf(baseName, "Laporan Daftar Donatur",
            new String[]{"ID", "Nama Donatur", "Kontak", "Telepon", "Email"},
            list, d -> new String[]{String.valueOf(d.getDonorId()), d.getDonorName(),
                d.getContact() != null ? d.getContact() : "-",
                d.getPhone() != null ? d.getPhone() : "-",
                d.getEmail() != null ? d.getEmail() : "-"});
    }

    private String generateEventPdf(List<Event> list, String baseName) throws Exception {
        return generatePdf(baseName, "Laporan Event Bencana",
            new String[]{"ID", "Nama Event", "Lokasi", "Status", "Deskripsi"},
            list, e -> new String[]{String.valueOf(e.getEventId()), e.getName(), e.getLocation(),
                e.getStatus(), e.getDescription() != null ? (e.getDescription().length() > 30 ? e.getDescription().substring(0, 30) + ".." : e.getDescription()) : "-"});
    }

    private <T> String generatePdf(String baseName, String title, String[] headers, List<T> items,
                                    java.util.function.Function<T, String[]> mapper) throws Exception {
        String filePath = getFilePath(baseName, ".pdf");
        try (PDDocument doc = new PDDocument()) {
            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont fontData = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float margin = 40;
            float yStart = 0;
            float tableWidth = PDRectangle.A4.getWidth() - 2 * margin;
            float rowHeight = 18;
            float cellMargin = 4;
            float colWidth = tableWidth / headers.length;
            int itemsPerPage = (int) ((PDRectangle.A4.getHeight() - 120) / rowHeight);
            int pageCount = (int) Math.ceil((double) items.size() / Math.max(1, itemsPerPage));

            for (int p = 0; p < pageCount; p++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    yStart = page.getMediaBox().getHeight() - margin;
                    float yPos = yStart;

                    // Title
                    cs.beginText(); cs.setFont(fontBold, 16);
                    cs.newLineAtOffset(margin, yPos); cs.showText("KEPO Command Center"); cs.endText();
                    yPos -= 22;

                    cs.beginText(); cs.setFont(fontData, 10);
                    cs.newLineAtOffset(margin, yPos); cs.showText(title); cs.endText();
                    yPos -= 18;

                    cs.beginText(); cs.setFont(fontData, 8);
                    cs.newLineAtOffset(margin, yPos);
                    cs.showText("Dibuat: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
                    cs.endText();
                    yPos -= 20;

                    // Header row
                    cs.setFont(fontBold, 9);
                    drawTableRow(cs, margin, yPos, colWidth, rowHeight, cellMargin, headers, fontBold, true);
                    yPos -= rowHeight;

                    cs.setStrokingColor(0f, 0f, 0f); cs.setLineWidth(0.5f);
                    cs.moveTo(margin, yPos); cs.lineTo(margin + tableWidth, yPos); cs.stroke();
                    yPos -= 2;

                    // Data rows for this page
                    int start = p * itemsPerPage;
                    int end = Math.min(start + itemsPerPage, items.size());
                    cs.setFont(fontData, 8);
                    for (int i = start; i < end; i++) {
                        String[] vals = mapper.apply(items.get(i));
                        drawTableRow(cs, margin, yPos, colWidth, rowHeight, cellMargin, vals, fontData, false);
                        yPos -= rowHeight;

                        cs.setStrokingColor(0.86f, 0.86f, 0.86f); cs.setLineWidth(0.3f);
                        cs.moveTo(margin, yPos); cs.lineTo(margin + tableWidth, yPos); cs.stroke();
                    }
                }
            }
            doc.save(filePath);
        }
        return filePath;
    }

    private void drawTableRow(PDPageContentStream cs, float x, float y, float colWidth, float rowHeight,
                               float cellMargin, String[] cells, PDFont font, boolean isHeader) throws Exception {
        for (int i = 0; i < cells.length; i++) {
            String text = cells[i] != null ? cells[i] : "";
            // Truncate long text
            float maxWidth = colWidth - 2 * cellMargin;
            if (font.getStringWidth(text) / 1000 * 9 > maxWidth) {
                while (font.getStringWidth(text + "..") / 1000 * 9 > maxWidth && text.length() > 2) {
                    text = text.substring(0, text.length() - 1);
                }
                text += "..";
            }
            float textX = x + i * colWidth + cellMargin;
            float textY = y - rowHeight + cellMargin + 5;
            cs.beginText();
            cs.setFont(font, isHeader ? 9 : 8);
            cs.newLineAtOffset(textX, textY);
            cs.showText(text);
            cs.endText();
        }
    }

    private String getFilePath(String baseName, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return outputDir + File.separator + baseName + "_" + timestamp + extension;
    }
}
