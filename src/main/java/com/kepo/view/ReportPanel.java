package com.kepo.view;

import com.kepo.service.ReportService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.File;

public class ReportPanel extends ScrollPane implements RefreshablePanel {

    private final ReportService reportService;
    private final MainLayout mainLayout;

    private VBox mainContainer;

    public ReportPanel(ReportService reportService, MainLayout mainLayout) {
        this.reportService = reportService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setFitToWidth(true);
        setFitToHeight(true);
        setStyle("-fx-background-color: transparent; -fx-background: " + ThemeConstants.BACKGROUND + ";");

        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(24));
        setContent(mainContainer);

        // --- Title ---
        Label title = new Label("Pusat Laporan & Pelaporan Darurat");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        mainContainer.getChildren().add(title);

        // --- Cards Vertical Stack ---
        VBox cardStack = new VBox(15);
        
        cardStack.getChildren().add(buildReportCard(
                "Laporan Pemantauan Shelter", 
                "Seluruh data posko penampungan korban beserta detail kapasitas terisi dan status siaga.", 
                "shelter"
        ));
        
        cardStack.getChildren().add(buildReportCard(
                "Laporan Registrasi Pengungsi", 
                "Data kependudukan demografis pengungsi aktif/keluar beserta catatan keluhan medis darurat.", 
                "refugee"
        ));
        
        cardStack.getChildren().add(buildReportCard(
                "Laporan Inventaris Logistik & Obat", 
                "Catatan persediaan obat-obatan kesehatan beserta nomor batch dan masa kedaluwarsa produk.", 
                "inventory"
        ));
        
        cardStack.getChildren().add(buildReportCard(
                "Laporan Alokasi & Distribusi Bantuan", 
                "Riwayat pengiriman bantuan logistik, makanan, pakaian, dan obat-obatan ke shelter.", 
                "distribution"
        ));
        
        cardStack.getChildren().add(buildReportCard(
                "Laporan Daftar Donatur", 
                "Direktori donatur aktif, instansi sponsor, kontak person, dan riwayat donasi bencana.", 
                "donor"
        ));
        
        cardStack.getChildren().add(buildReportCard(
                "Laporan Riwayat Operasi Bencana", 
                "Daftar log penanggulangan kejadian bencana yang pernah dan sedang aktif ditangani.", 
                "event"
        ));

        mainContainer.getChildren().add(cardStack);
    }

    private Pane buildReportCard(String name, String description, String type) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(ThemeConstants.CARD_STYLE);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        descLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
        descLabel.setWrapText(true);

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button htmlBtn = new Button("HTML / PDF Preview");
        htmlBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        htmlBtn.setOnAction(e -> generateReportFile(type, "PDF"));

        Button excelBtn = new Button("Excel Export");
        excelBtn.setStyle(ThemeConstants.SECONDARY_BTN_STYLE);
        excelBtn.setOnAction(e -> generateReportFile(type, "EXCEL"));

        btnRow.getChildren().addAll(htmlBtn, excelBtn);

        card.getChildren().addAll(nameLabel, descLabel, btnRow);
        return card;
    }

    private void generateReportFile(String type, String format) {
        try {
            String filePath = switch (type) {
                case "shelter" -> reportService.generateShelterReport(format);
                case "refugee" -> reportService.generateRefugeeReport(format);
                case "inventory" -> reportService.generateInventoryReport(format);
                case "distribution" -> reportService.generateDistributionReport(format);
                case "donor" -> reportService.generateDonorReport(format);
                case "event" -> reportService.generateEventReport(format);
                default -> throw new IllegalArgumentException("Jenis laporan tidak valid.");
            };

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Laporan Berhasil Dibuat");
            alert.setHeaderText("Sukses Mengompilasi Laporan");
            alert.setContentText("Berkas berhasil disimpan ke folder output:\n" + filePath);
            alert.showAndWait();

            // Auto open the file on desktop
            File file = new File(filePath);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gagal Mengompilasi Laporan");
            alert.setHeaderText("Error Pembuatan Laporan");
            alert.setContentText("Detail kegagalan: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void refreshData() {
        // No data variables to reload in index page
    }
}
