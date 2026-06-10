package com.pharmastock.service;

import com.pharmastock.config.DatabaseConfig;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ReportService {

    private final DatabaseConfig db;
    private final String outputDir;

    public ReportService(DatabaseConfig db, String outputDir) {
        this.db = db;
        this.outputDir = outputDir;
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String generateInventoryReport(String format) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_TITLE", "Laporan Inventaris Obat");
        params.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));
        return generateReport("/reports/inventory_report.jrxml", params, "inventory_report", format);
    }

    public String generateExpiredReport(String format) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_TITLE", "Laporan Obat Kadaluarsa");
        params.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));
        return generateReport("/reports/expired_report.jrxml", params, "expired_report", format);
    }

    public String generateStockMovementReport(LocalDate from, LocalDate to, String format) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_TITLE", "Laporan Pergerakan Stok");
        params.put("DATE_FROM", java.sql.Date.valueOf(from));
        params.put("DATE_TO", java.sql.Date.valueOf(to));
        params.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));
        return generateReport("/reports/stock_movement_report.jrxml", params, "stock_movement_report", format);
    }

    public String generateSupplierReport(String format) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_TITLE", "Laporan Supplier");
        params.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));
        return generateReport("/reports/supplier_report.jrxml", params, "supplier_report", format);
    }

    private String generateReport(String templatePath, Map<String, Object> params,
                                  String baseName, String format) throws Exception {
        InputStream is = getClass().getResourceAsStream(templatePath);
        if (is == null) {
            throw new Exception("Template laporan tidak ditemukan: " + templatePath);
        }

        JasperReport report = JasperCompileManager.compileReport(is);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = baseName + "_" + timestamp;

        try (Connection conn = db.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);

            if ("PDF".equalsIgnoreCase(format)) {
                String filePath = outputDir + File.separator + fileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(print, filePath);
                return filePath;
            } else if ("EXCEL".equalsIgnoreCase(format) || "XLSX".equalsIgnoreCase(format)) {
                String filePath = outputDir + File.separator + fileName + ".xlsx";
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(filePath));
                SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
                config.setOnePagePerSheet(false);
                config.setDetectCellType(true);
                exporter.setConfiguration(config);
                exporter.exportReport();
                return filePath;
            } else {
                throw new IllegalArgumentException("Format tidak didukung: " + format);
            }
        }
    }
}
