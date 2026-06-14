package com.kepo.view;

import com.kepo.controller.DashboardController;
import com.kepo.model.AuditLog;
import com.kepo.model.Distribution;
import com.kepo.model.Shelter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class DashboardView extends ScrollPane implements RefreshablePanel {

    private final DashboardController controller;
    private final MainLayout mainLayout;

    private VBox mainContainer;
    private Label totalSheltersVal;
    private Label totalRefugeesVal;
    private Label criticalSheltersVal;
    private Label activeEventsVal;

    private BarChart<String, Number> shelterChart;
    private XYChart.Series<String, Number> occupancySeries;
    private XYChart.Series<String, Number> capacitySeries;

    private VBox alertsContainer;
    private VBox distributionContainer;
    private VBox aiRecContainer;

    public DashboardView(DashboardController controller, MainLayout mainLayout) {
        this.controller = controller;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setFitToWidth(true);
        setFitToHeight(true);
        setStyle("-fx-background-color: transparent; -fx-background: " + ThemeConstants.BACKGROUND + ";");

        mainContainer = new VBox(24);
        mainContainer.setPadding(new Insets(24));
        setContent(mainContainer);

        // --- Header Section ---
        Label title = new Label("Pusat Komando Operasional (Command Center)");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        mainContainer.getChildren().add(title);

        // --- Row 1: KPI Cards Grid (4 columns) ---
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(16);
        kpiGrid.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            kpiGrid.getColumnConstraints().add(col);
        }

        Pane shelterCard = createKpiCard("Total Shelter Evakuasi", totalSheltersVal = new Label("0"), ThemeConstants.SECONDARY);
        Pane refugeeCard = createKpiCard("Total Pengungsi Aktif", totalRefugeesVal = new Label("0"), "#6366f1");
        Pane criticalCard = createKpiCard("Shelter Status Kritis", criticalSheltersVal = new Label("0"), ThemeConstants.DANGER);
        Pane eventCard = createKpiCard("Kejadian Bencana Aktif", activeEventsVal = new Label("0"), ThemeConstants.PRIMARY);

        kpiGrid.add(shelterCard, 0, 0);
        kpiGrid.add(refugeeCard, 1, 0);
        kpiGrid.add(criticalCard, 2, 0);
        kpiGrid.add(eventCard, 3, 0);

        mainContainer.getChildren().add(kpiGrid);

        // --- Row 2: Charts & Alerts Split Layout ---
        GridPane row2Grid = new GridPane();
        row2Grid.setHgap(20);
        row2Grid.setVgap(20);
        
        ColumnConstraints r2Left = new ColumnConstraints();
        r2Left.setPercentWidth(60);
        ColumnConstraints r2Right = new ColumnConstraints();
        r2Right.setPercentWidth(40);
        row2Grid.getColumnConstraints().addAll(r2Left, r2Right);

        // Baris 2 Left: Shelter Capacity Chart
        VBox chartBox = createDashboardBox("Kapasitas & Kepadatan Shelter");
        chartBox.getChildren().add(createShelterChart());
        row2Grid.add(chartBox, 0, 0);

        // Baris 2 Right: Emergency Alert Center
        VBox alertBox = createDashboardBox("Pusat Peringatan Darurat");
        alertsContainer = new VBox(10);
        alertsContainer.setPrefHeight(300);
        alertBox.getChildren().add(alertsContainer);
        row2Grid.add(alertBox, 1, 0);

        mainContainer.getChildren().add(row2Grid);

        // --- Row 3: Distributions & AI Recommendations Split Layout ---
        GridPane row3Grid = new GridPane();
        row3Grid.setHgap(20);
        row3Grid.setVgap(20);
        
        ColumnConstraints r3Left = new ColumnConstraints();
        r3Left.setPercentWidth(60);
        ColumnConstraints r3Right = new ColumnConstraints();
        r3Right.setPercentWidth(40);
        row3Grid.getColumnConstraints().addAll(r3Left, r3Right);

        // Baris 3 Left: Distribution Status list
        VBox distBox = createDashboardBox("Status Alokasi & Distribusi Logistik");
        distributionContainer = new VBox(10);
        distributionContainer.setPrefHeight(250);
        distBox.getChildren().add(distributionContainer);
        row3Grid.add(distBox, 0, 0);

        // Baris 3 Right: AI Decision Recommendations
        VBox aiBox = createDashboardBox("Rekomendasi Pintar AI");
        aiRecContainer = new VBox(10);
        aiRecContainer.setPrefHeight(250);
        aiBox.getChildren().add(aiRecContainer);
        row3Grid.add(aiBox, 1, 0);

        mainContainer.getChildren().add(row3Grid);

        // Initial Data Populate
        refreshData();
    }

    private Pane createKpiCard(String title, Label valueLabel, String accentColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(ThemeConstants.CARD_STYLE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));

        valueLabel.setFont(Font.font("Plus Jakarta Sans", FontWeight.BLACK, 28));
        valueLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region line = new Region();
        line.setPrefHeight(4);
        line.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");

        card.getChildren().addAll(titleLabel, valueLabel, line);
        return card;
    }

    private VBox createDashboardBox(String title) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(16));
        box.setStyle(ThemeConstants.CARD_STYLE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        box.getChildren().add(titleLabel);
        return box;
    }

    private BarChart<String, Number> createShelterChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        shelterChart = new BarChart<>(xAxis, yAxis);
        shelterChart.setLegendVisible(true);
        shelterChart.setPrefHeight(300);
        shelterChart.setStyle("-fx-background-color: transparent;");

        occupancySeries = new XYChart.Series<>();
        occupancySeries.setName("Terisi");

        capacitySeries = new XYChart.Series<>();
        capacitySeries.setName("Kapasitas");

        shelterChart.getData().addAll(occupancySeries, capacitySeries);
        return shelterChart;
    }

    @Override
    public void refreshData() {
        // Refresh counter labels
        totalSheltersVal.setText(String.valueOf(controller.getTotalSheltersCount()));
        totalRefugeesVal.setText(String.valueOf(controller.getTotalRefugeesCount()));
        criticalSheltersVal.setText(String.valueOf(controller.getCriticalSheltersCount()));
        activeEventsVal.setText(String.valueOf(controller.getActiveEventsCount()));

        // Update Shelter Chart Data
        occupancySeries.getData().clear();
        capacitySeries.getData().clear();
        List<Shelter> shelters = controller.getShelters();
        for (Shelter s : shelters) {
            occupancySeries.getData().add(new XYChart.Data<>(s.getName(), s.getCurrentOccupancy()));
            capacitySeries.getData().add(new XYChart.Data<>(s.getName(), s.getCapacity()));
        }

        // Populate Alerts
        alertsContainer.getChildren().clear();
        List<String> alerts = controller.getEmergencyAlerts();
        if (alerts.isEmpty()) {
            Label label = new Label("Tidak ada peringatan darurat saat ini.");
            label.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
            label.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
            alertsContainer.getChildren().add(label);
        } else {
            // Keep at most 8 alerts
            int limit = Math.min(alerts.size(), 8);
            for (int i = 0; i < limit; i++) {
                String alert = alerts.get(i);
                Label label = new Label(alert);
                label.setWrapText(true);
                label.setFont(Font.font("Inter", FontWeight.BOLD, 12));
                if (alert.contains("[SHELTER KRITIS]") || alert.contains("[STOK KRITIS]")) {
                    label.setTextFill(Color.web(ThemeConstants.DANGER));
                } else if (alert.contains("WASPADA")) {
                    label.setTextFill(Color.web(ThemeConstants.WARNING));
                } else {
                    label.setTextFill(Color.web(ThemeConstants.PRIMARY));
                }
                alertsContainer.getChildren().add(label);
            }
        }

        // Populate Active Distributions
        distributionContainer.getChildren().clear();
        List<Distribution> dists = controller.getDistributions();
        if (dists.isEmpty()) {
            Label label = new Label("Tidak ada pengiriman logistik bantuan.");
            label.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
            label.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
            distributionContainer.getChildren().add(label);
        } else {
            // Keep at most 6 distributions
            int limit = Math.min(dists.size(), 6);
            for (int i = 0; i < limit; i++) {
                Distribution d = dists.get(i);
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 0, 4, 0));
                
                Label docLbl = new Label(d.getDocNum());
                docLbl.setFont(Font.font("Inter", FontWeight.BOLD, 12));
                docLbl.setTextFill(Color.web(ThemeConstants.PRIMARY));
                docLbl.setPrefWidth(140);

                Label shelterLbl = new Label(d.getShelterName());
                shelterLbl.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
                shelterLbl.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
                shelterLbl.setPrefWidth(140);

                Label detailLbl = new Label(d.getItemType() + " (" + d.getQuantity() + ")");
                detailLbl.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
                detailLbl.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
                detailLbl.setPrefWidth(120);

                Label statusLbl = new Label(d.getStatus());
                statusLbl.setFont(Font.font("Inter", FontWeight.BOLD, 11));
                
                String color = switch (d.getStatus()) {
                    case "RECEIVED" -> ThemeConstants.SECONDARY;
                    case "SHIPPED" -> "#3b82f6";
                    case "APPROVED" -> ThemeConstants.WARNING;
                    default -> ThemeConstants.ON_SURFACE_VARIANT;
                };
                statusLbl.setTextFill(Color.web(color));

                row.getChildren().addAll(docLbl, shelterLbl, detailLbl, statusLbl);
                distributionContainer.getChildren().add(row);
            }
        }

        // Populate AI recommendations
        aiRecContainer.getChildren().clear();
        List<String> suggestions = controller.getAISuggestions();
        for (String suggestion : suggestions) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.TOP_LEFT);
            Label dot = new Label("•");
            dot.setTextFill(Color.web(ThemeConstants.PRIMARY_LIGHT));
            Label text = new Label(suggestion);
            text.setWrapText(true);
            text.setFont(Font.font("Inter", FontWeight.BOLD, 12));
            text.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
            HBox.setHgrow(text, Priority.ALWAYS);
            row.getChildren().addAll(dot, text);
            aiRecContainer.getChildren().add(row);
        }
    }
}
