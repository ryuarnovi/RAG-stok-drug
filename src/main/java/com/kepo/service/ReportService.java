package com.kepo.service;

import com.kepo.model.*;
import com.kepo.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
        if ("EXCEL".equalsIgnoreCase(format)) {
            return generateShelterExcel(data, baseName);
        } else {
            return generateShelterHtml(data, baseName);
        }
    }

    public String generateRefugeeReport(String format) throws Exception {
        List<Refugee> data = refugeeRepo.findAll();
        String baseName = "laporan_pengungsi";
        if ("EXCEL".equalsIgnoreCase(format)) {
            return generateRefugeeExcel(data, baseName);
        } else {
            return generateRefugeeHtml(data, baseName);
        }
    }

    public String generateInventoryReport(String format) throws Exception {
        List<Medicine> data = medicineRepo.findAll();
        String baseName = "laporan_inventaris_obat";
        if ("EXCEL".equalsIgnoreCase(format)) {
            return generateInventoryExcel(data, baseName);
        } else {
            return generateInventoryHtml(data, baseName);
        }
    }

    public String generateDistributionReport(String format) throws Exception {
        List<Distribution> data = distributionRepo.findAll();
        String baseName = "laporan_distribusi_bantuan";
        if ("EXCEL".equalsIgnoreCase(format)) {
            return generateDistributionExcel(data, baseName);
        } else {
            return generateDistributionHtml(data, baseName);
        }
    }

    public String generateDonorReport(String format) throws Exception {
        List<Donor> data = donorRepo.findAll();
        String baseName = "laporan_donatur";
        if ("EXCEL".equalsIgnoreCase(format)) {
            return generateDonorExcel(data, baseName);
        } else {
            return generateDonorHtml(data, baseName);
        }
    }

    public String generateEventReport(String format) throws Exception {
        List<Event> data = eventRepo.findAll();
        String baseName = "laporan_event_bencana";
        if ("EXCEL".equalsIgnoreCase(format)) {
            return generateEventExcel(data, baseName);
        } else {
            return generateEventHtml(data, baseName);
        }
    }

    // --- EXCEL GENERATORS ---

    private String generateShelterExcel(List<Shelter> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Shelter");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama Shelter", "Lokasi", "Kapasitas", "Terisi", "Status", "Penanggung Jawab"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
        return filePath;
    }

    private String generateRefugeeExcel(List<Refugee> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Pengungsi");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama", "NIK", "Usia", "Gender", "Status", "Shelter", "Catatan Medis", "Waktu Masuk", "Waktu Keluar"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
        return filePath;
    }

    private String generateInventoryExcel(List<Medicine> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inventaris Obat");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Kode Obat", "Nama Obat", "Kategori", "Batch", "Unit", "Stok", "Min Stok", "Harga Beli", "Harga Jual", "Expiry Date", "Supplier"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
        return filePath;
    }

    private String generateDistributionExcel(List<Distribution> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Distribusi");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "No Dokumen", "Shelter Tujuan", "Tipe Bantuan", "Jumlah", "Status", "Keterangan"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
        return filePath;
    }

    private String generateDonorExcel(List<Donor> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Donatur");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama Donatur", "Kontak", "Telepon", "Email", "Alamat"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
        return filePath;
    }

    private String generateEventExcel(List<Event> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Event Bencana");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Nama Event", "Lokasi", "Status", "Deskripsi", "Waktu Mulai"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
        return filePath;
    }

    // --- HTML GENERATORS ---

    private String generateShelterHtml(List<Shelter> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".html");
        StringBuilder sb = new StringBuilder();
        buildHtmlHeader(sb, "Laporan Pemantauan Shelter");
        sb.append("<table class='min-w-full border-collapse'><thead><tr>")
          .append("<th class='border px-4 py-2 bg-gray-100'>ID</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Nama Shelter</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Lokasi</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Kapasitas</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Terisi</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Status</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Penanggung Jawab</th>")
          .append("</tr></thead><tbody>");
        for (Shelter s : list) {
            sb.append("<tr>")
              .append("<td class='border px-4 py-2 text-center'>").append(s.getShelterId()).append("</td>")
              .append("<td class='border px-4 py-2 font-semibold'>").append(s.getName()).append("</td>")
              .append("<td class='border px-4 py-2'>").append(s.getLocation()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(s.getCapacity()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(s.getCurrentOccupancy()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'><span class='px-2 py-1 rounded text-xs font-bold bg-blue-100'>").append(s.getStatus()).append("</span></td>")
              .append("<td class='border px-4 py-2'>").append(s.getPenanggungJawab()).append("</td>")
              .append("</tr>");
        }
        buildHtmlFooter(sb);
        writeHtmlFile(filePath, sb.toString());
        return filePath;
    }

    private String generateRefugeeHtml(List<Refugee> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".html");
        StringBuilder sb = new StringBuilder();
        buildHtmlHeader(sb, "Laporan Registrasi Pengungsi");
        sb.append("<table class='min-w-full border-collapse'><thead><tr>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Nama</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>NIK</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Usia</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Gender</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Status</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Shelter</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Catatan Medis</th>")
          .append("</tr></thead><tbody>");
        for (Refugee ref : list) {
            sb.append("<tr>")
              .append("<td class='border px-4 py-2 font-semibold'>").append(ref.getName()).append("</td>")
              .append("<td class='border px-4 py-2'>").append(ref.getNik()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(ref.getAge()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(ref.getGender()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'><span class='px-2 py-1 rounded text-xs font-bold bg-green-100'>").append(ref.getStatus()).append("</span></td>")
              .append("<td class='border px-4 py-2'>").append(ref.getShelterName() != null ? ref.getShelterName() : "N/A").append("</td>")
              .append("<td class='border px-4 py-2 text-red-600'>").append(ref.getMedicalNotes() != null ? ref.getMedicalNotes() : "-").append("</td>")
              .append("</tr>");
        }
        buildHtmlFooter(sb);
        writeHtmlFile(filePath, sb.toString());
        return filePath;
    }

    private String generateInventoryHtml(List<Medicine> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".html");
        StringBuilder sb = new StringBuilder();
        buildHtmlHeader(sb, "Laporan Inventaris Logistik & Obat-obatan");
        sb.append("<table class='min-w-full border-collapse'><thead><tr>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Kode</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Nama Obat</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Kategori</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Stok</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Min</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Exp Date</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Supplier</th>")
          .append("</tr></thead><tbody>");
        for (Medicine m : list) {
            sb.append("<tr>")
              .append("<td class='border px-4 py-2 text-center'>").append(m.getMedicineCode()).append("</td>")
              .append("<td class='border px-4 py-2 font-semibold'>").append(m.getMedicineName()).append("</td>")
              .append("<td class='border px-4 py-2'>").append(m.getCategory()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(m.getStockQuantity()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(m.getMinimumStock()).append("</td>")
              .append("<td class='border px-4 py-2 text-center text-red-500'>").append(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "-").append("</td>")
              .append("<td class='border px-4 py-2'>").append(m.getSupplierName() != null ? m.getSupplierName() : "N/A").append("</td>")
              .append("</tr>");
        }
        buildHtmlFooter(sb);
        writeHtmlFile(filePath, sb.toString());
        return filePath;
    }

    private String generateDistributionHtml(List<Distribution> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".html");
        StringBuilder sb = new StringBuilder();
        buildHtmlHeader(sb, "Laporan Alokasi & Distribusi Bantuan");
        sb.append("<table class='min-w-full border-collapse'><thead><tr>")
          .append("<th class='border px-4 py-2 bg-gray-100'>No Dokumen</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Shelter Tujuan</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Tipe</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Jumlah</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Status</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Keterangan</th>")
          .append("</tr></thead><tbody>");
        for (Distribution d : list) {
            sb.append("<tr>")
              .append("<td class='border px-4 py-2 text-center font-mono'>").append(d.getDocNum()).append("</td>")
              .append("<td class='border px-4 py-2'>").append(d.getShelterName() != null ? d.getShelterName() : "N/A").append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(d.getItemType()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'>").append(d.getQuantity()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'><span class='px-2 py-1 rounded text-xs font-bold bg-yellow-100'>").append(d.getStatus()).append("</span></td>")
              .append("<td class='border px-4 py-2'>").append(d.getNotes() != null ? d.getNotes() : "-").append("</td>")
              .append("</tr>");
        }
        buildHtmlFooter(sb);
        writeHtmlFile(filePath, sb.toString());
        return filePath;
    }

    private String generateDonorHtml(List<Donor> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".html");
        StringBuilder sb = new StringBuilder();
        buildHtmlHeader(sb, "Laporan Daftar Donatur");
        sb.append("<table class='min-w-full border-collapse'><thead><tr>")
          .append("<th class='border px-4 py-2 bg-gray-100'>ID</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Nama Donatur</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Kontak</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Telepon</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Email</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Alamat</th>")
          .append("</tr></thead><tbody>");
        for (Donor d : list) {
            sb.append("<tr>")
              .append("<td class='border px-4 py-2 text-center'>").append(d.getDonorId()).append("</td>")
              .append("<td class='border px-4 py-2 font-semibold'>").append(d.getDonorName()).append("</td>")
              .append("<td class='border px-4 py-2'>").append(d.getContact() != null ? d.getContact() : "-").append("</td>")
              .append("<td class='border px-4 py-2'>").append(d.getPhone() != null ? d.getPhone() : "-").append("</td>")
              .append("<td class='border px-4 py-2'>").append(d.getEmail() != null ? d.getEmail() : "-").append("</td>")
              .append("<td class='border px-4 py-2'>").append(d.getAddress() != null ? d.getAddress() : "-").append("</td>")
              .append("</tr>");
        }
        buildHtmlFooter(sb);
        writeHtmlFile(filePath, sb.toString());
        return filePath;
    }

    private String generateEventHtml(List<Event> list, String baseName) throws Exception {
        String filePath = getFilePath(baseName, ".html");
        StringBuilder sb = new StringBuilder();
        buildHtmlHeader(sb, "Laporan Riwayat Operasi Bencana");
        sb.append("<table class='min-w-full border-collapse'><thead><tr>")
          .append("<th class='border px-4 py-2 bg-gray-100'>ID</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Nama Event</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Lokasi</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Status</th>")
          .append("<th class='border px-4 py-2 bg-gray-100'>Deskripsi</th>")
          .append("</tr></thead><tbody>");
        for (Event e : list) {
            sb.append("<tr>")
              .append("<td class='border px-4 py-2 text-center'>").append(e.getEventId()).append("</td>")
              .append("<td class='border px-4 py-2 font-semibold'>").append(e.getName()).append("</td>")
              .append("<td class='border px-4 py-2'>").append(e.getLocation()).append("</td>")
              .append("<td class='border px-4 py-2 text-center'><span class='px-2 py-1 rounded text-xs font-bold bg-red-100'>").append(e.getStatus()).append("</span></td>")
              .append("<td class='border px-4 py-2'>").append(e.getDescription() != null ? e.getDescription() : "-").append("</td>")
              .append("</tr>");
        }
        buildHtmlFooter(sb);
        writeHtmlFile(filePath, sb.toString());
        return filePath;
    }

    // --- HTML STYLING HELPERS ---

    private void buildHtmlHeader(StringBuilder sb, String title) {
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'/>")
          .append("<title>").append(title).append("</title>")
          .append("<script src='https://cdn.tailwindcss.com'></script>")
          .append("</head><body class='bg-gray-50 text-gray-800 p-8'>")
          .append("<div class='max-w-6xl mx-auto bg-white p-6 rounded-lg shadow-sm border border-gray-200'>")
          .append("<div class='border-b pb-4 mb-6 flex justify-between items-center'>")
          .append("<div><h1 class='text-2xl font-bold text-sky-800'>KEPO Command Center</h1>")
          .append("<p class='text-sm text-gray-500'>").append(title).append("</p></div>")
          .append("<div class='text-right text-xs text-gray-400'>")
          .append("Dibuat pada: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")))
          .append("</div></div>");
    }

    private void buildHtmlFooter(StringBuilder sb) {
        sb.append("</tbody></table>")
          .append("<div class='border-t pt-4 mt-6 text-center text-xs text-gray-400'>")
          .append("KEPO &copy; 2026 - Pusat Koordinasi Operasional Kebencanaan Darurat")
          .append("</div></div></body></html>");
    }

    private void writeHtmlFile(String path, String content) throws Exception {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(content);
        }
    }

    private String getFilePath(String baseName, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return outputDir + File.separator + baseName + "_" + timestamp + extension;
    }
}
