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
import javafx.scene.control.Separator;
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
    private Label fullSheltersVal;
    private Label availableSheltersVal;
    private Label criticalLogisticsVal;

    private BarChart<String, Number> shelterChart;
    private XYChart.Series<String, Number> occupancySeries;
    private XYChart.Series<String, Number> capacitySeries;

    private VBox alertsContainer;
    private VBox distributionContainer;
    private VBox aiRecContainer;
    
    // New UI controls
    private VBox prioritiesContainer;
    private VBox topLogisticsContainer;
    private javafx.scene.canvas.Canvas mapCanvas;

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

        // --- Row 1: KPI Cards Grid (6 columns, 2 rows of 3 or single row) ---
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(16);
        kpiGrid.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.3);
            kpiGrid.getColumnConstraints().add(col);
        }

        Pane shelterCard = createKpiCard("Total Shelter Evakuasi", totalSheltersVal = new Label("0"), ThemeConstants.SECONDARY);
        Pane refugeeCard = createKpiCard("Total Pengungsi Aktif", totalRefugeesVal = new Label("0"), "#6366f1");
        Pane criticalCard = createKpiCard("Shelter Kritis (Rasio)", criticalSheltersVal = new Label("0"), ThemeConstants.DANGER);
        Pane eventCard = createKpiCard("Bencana Aktif", activeEventsVal = new Label("0"), ThemeConstants.PRIMARY);
        Pane fullCard = createKpiCard("Shelter Penuh", fullSheltersVal = new Label("0"), "#f59e0b");
        Pane logisticsCard = createKpiCard("Kritis Logistik", criticalLogisticsVal = new Label("0"), "#8b5cf6");

        kpiGrid.add(shelterCard, 0, 0);
        kpiGrid.add(refugeeCard, 1, 0);
        kpiGrid.add(criticalCard, 2, 0);
        kpiGrid.add(eventCard, 0, 1);
        kpiGrid.add(fullCard, 1, 1);
        kpiGrid.add(logisticsCard, 2, 1);

        mainContainer.getChildren().add(kpiGrid);

        // --- Row 2: Charts & Alerts Split Layout ---
        GridPane row2Grid = new GridPane();
        row2Grid.setHgap(20);
        row2Grid.setVgap(20);
        
        ColumnConstraints r2Left = new ColumnConstraints();
        r2Left.setPercentWidth(55);
        ColumnConstraints r2Right = new ColumnConstraints();
        r2Right.setPercentWidth(45);
        row2Grid.getColumnConstraints().addAll(r2Left, r2Right);

        // Left: Shelter Capacity Chart
        VBox chartBox = createDashboardBox("Kapasitas & Kepadatan Shelter");
        chartBox.getChildren().add(createShelterChart());
        row2Grid.add(chartBox, 0, 0);

        // Right: Emergency Alert Center & Priorities
        VBox alertBox = createDashboardBox("Status Kebutuhan & Kelompok Prioritas");
        prioritiesContainer = new VBox(8);
        prioritiesContainer.setPadding(new Insets(5, 0, 5, 0));
        
        alertsContainer = new VBox(8);
        alertsContainer.setPrefHeight(150);
        
        alertBox.getChildren().addAll(new Label("Pusat Peringatan Darurat:"), alertsContainer, new Separator(), new Label("Klasifikasi Prioritas Pengungsi:"), prioritiesContainer);
        row2Grid.add(alertBox, 1, 0);

        mainContainer.getChildren().add(row2Grid);

        // --- Row 3: Map & Top Logistics Split Layout ---
        GridPane row3Grid = new GridPane();
        row3Grid.setHgap(20);
        row3Grid.setVgap(20);
        
        ColumnConstraints r3Left = new ColumnConstraints();
        r3Left.setPercentWidth(55);
        ColumnConstraints r3Right = new ColumnConstraints();
        r3Right.setPercentWidth(45);
        row3Grid.getColumnConstraints().addAll(r3Left, r3Right);

        // Left: Map Visual Box
        VBox mapBox = createDashboardBox("Peta Sebaran Shelter (Visual)");
        mapCanvas = new javafx.scene.canvas.Canvas(420, 220);
        mapBox.getChildren().add(mapCanvas);
        row3Grid.add(mapBox, 0, 0);

        // Right: Top Needy Shelters List
        VBox needyBox = createDashboardBox("Kesiapan Logistik Shelter Terendah");
        topLogisticsContainer = new VBox(8);
        needyBox.getChildren().add(topLogisticsContainer);
        row3Grid.add(needyBox, 1, 0);

        mainContainer.getChildren().add(row3Grid);

        // --- Row 4: Distributions & AI Recommendations Split Layout ---
        GridPane row4Grid = new GridPane();
        row4Grid.setHgap(20);
        row4Grid.setVgap(20);
        
        ColumnConstraints r4Left = new ColumnConstraints();
        r4Left.setPercentWidth(55);
        ColumnConstraints r4Right = new ColumnConstraints();
        r4Right.setPercentWidth(45);
        row4Grid.getColumnConstraints().addAll(r4Left, r4Right);

        // Left: Distribution Status list
        VBox distBox = createDashboardBox("Status Alokasi & Distribusi Logistik");
        distributionContainer = new VBox(10);
        distributionContainer.setPrefHeight(200);
        distBox.getChildren().add(distributionContainer);
        row4Grid.add(distBox, 0, 0);

        // Right: AI Decision Recommendations
        VBox aiBox = createDashboardBox("Rekomendasi Pintar AI");
        aiRecContainer = new VBox(10);
        aiRecContainer.setPrefHeight(200);
        aiBox.getChildren().add(aiRecContainer);
        row4Grid.add(aiBox, 1, 0);

        mainContainer.getChildren().add(row4Grid);

        // Initial Data Populate
        refreshData();
    }

    private Pane createKpiCard(String title, Label valueLabel, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle(ThemeConstants.CARD_STYLE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));

        valueLabel.setFont(Font.font("Plus Jakarta Sans", FontWeight.BLACK, 24));
        valueLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region line = new Region();
        line.setPrefHeight(4);
        line.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");

        card.getChildren().addAll(titleLabel, valueLabel, line);
        return card;
    }

    private VBox createDashboardBox(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle(ThemeConstants.CARD_STYLE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        box.getChildren().add(titleLabel);
        return box;
    }

    private BarChart<String, Number> createShelterChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        shelterChart = new BarChart<>(xAxis, yAxis);
        shelterChart.setLegendVisible(true);
        shelterChart.setPrefHeight(260);
        shelterChart.setStyle("-fx-background-color: transparent;");

        occupancySeries = new XYChart.Series<>();
        occupancySeries.setName("Terisi");

        capacitySeries = new XYChart.Series<>();
        capacitySeries.setName("Kapasitas");

        shelterChart.getData().addAll(occupancySeries, capacitySeries);
        return shelterChart;
    }

    private void drawMap(List<Shelter> shelters) {
        javafx.scene.canvas.GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Draw light background map outline/grid
        gc.setStroke(Color.web(ThemeConstants.BORDER));
        gc.setLineWidth(1);
        for (int i = 0; i < mapCanvas.getWidth(); i += 40) {
            gc.strokeLine(i, 0, i, mapCanvas.getHeight());
        }
        for (int j = 0; j < mapCanvas.getHeight(); j += 40) {
            gc.strokeLine(0, j, mapCanvas.getWidth(), j);
        }

        // Render mock island shapes
        gc.setFill(Color.web("#f1f5f9"));
        gc.fillRoundRect(30, 20, 160, 100, 20, 20);
        gc.fillRoundRect(220, 80, 150, 100, 25, 25);

        // Draw status dots for shelters
        int xSeed = 45;
        int ySeed = 50;
        for (Shelter s : shelters) {
            Color color;
            if ("KRITIS".equals(s.getStatus()) || s.getCurrentOccupancy() >= s.getCapacity()) {
                color = Color.web(ThemeConstants.DANGER);
            } else if ("WASPADA".equals(s.getStatus())) {
                color = Color.web(ThemeConstants.WARNING);
            } else {
                color = Color.web(ThemeConstants.SECONDARY);
            }

            gc.setFill(color);
            gc.fillOval(xSeed, ySeed, 12, 12);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeOval(xSeed, ySeed, 12, 12);

            gc.setFill(Color.web(ThemeConstants.ON_SURFACE));
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 10));
            gc.fillText(s.getName(), xSeed + 16, ySeed + 10);

            xSeed = (xSeed + 85) % (int) (mapCanvas.getWidth() - 100);
            ySeed = (ySeed + 60) % (int) (mapCanvas.getHeight() - 40);
            if (xSeed < 30) xSeed = 40;
            if (ySeed < 30) ySeed = 50;
        }
    }

    @Override
    public void refreshData() {
        // Refresh counter labels
        totalSheltersVal.setText(String.valueOf(controller.getTotalSheltersCount()));
        totalRefugeesVal.setText(String.valueOf(controller.getTotalRefugeesCount()));
        criticalSheltersVal.setText(String.format("%d Posko", controller.getCriticalSheltersCount()));
        activeEventsVal.setText(String.valueOf(controller.getActiveEventsCount()));
        fullSheltersVal.setText(String.valueOf(controller.getFullSheltersCount()));
        criticalLogisticsVal.setText(String.format("%d Posko", controller.getCriticalLogisticsSheltersCount()));

        // Update Shelter Chart Data
        occupancySeries.getData().clear();
        capacitySeries.getData().clear();
        List<Shelter> shelters = controller.getShelters();
        for (Shelter s : shelters) {
            occupancySeries.getData().add(new XYChart.Data<>(s.getName(), s.getCurrentOccupancy()));
            capacitySeries.getData().add(new XYChart.Data<>(s.getName(), s.getCapacity()));
        }

        // Draw Map
        drawMap(shelters);

        // Populate Priorities List
        prioritiesContainer.getChildren().clear();
        String[] priorities = {"LANSIA", "BALITA", "IBU_HAMIL", "DISABILITAS", "SICK"};
        String[] labels = {"Lansia (60+ th)", "Balita (<5 th)", "Ibu Hamil", "Disabilitas", "Sakit/Kasus Medis"};
        for (int i = 0; i < priorities.length; i++) {
            String p = priorities[i];
            int count = controller.getRefugeePriorityCount(p);
            
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label pLbl = new Label(labels[i]);
            pLbl.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            pLbl.setPrefWidth(120);

            Label cntLbl = new Label(String.valueOf(count) + " Jiwa");
            cntLbl.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 11px;");

            row.getChildren().addAll(pLbl, cntLbl);
            prioritiesContainer.getChildren().add(row);
        }

        // Populate Top Logistics Low Availability
        topLogisticsContainer.getChildren().clear();
        List<Shelter> needy = controller.getTopLogisticNeedyShelters();
        if (needy.isEmpty()) {
            Label label = new Label("Ketersediaan logistik semua shelter terpantau aman.");
            label.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-style: italic; -fx-font-size: 12px;");
            topLogisticsContainer.getChildren().add(label);
        } else {
            for (Shelter s : needy) {
                double avg = controller.getAverageAvailability(s.getShelterId());
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLbl = new Label(s.getName());
                nameLbl.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 12px;");
                nameLbl.setPrefWidth(180);

                Label valLbl = new Label(String.format("%.1f%% Ketersediaan", avg));
                if (avg < 50) {
                    valLbl.setStyle("-fx-text-fill: " + ThemeConstants.DANGER + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                } else if (avg < 80) {
                    valLbl.setStyle("-fx-text-fill: " + ThemeConstants.WARNING + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                } else {
                    valLbl.setStyle("-fx-text-fill: " + ThemeConstants.SECONDARY + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                }

                row.getChildren().addAll(nameLbl, valLbl);
                topLogisticsContainer.getChildren().add(row);
            }
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
            int limit = Math.min(alerts.size(), 4);
            for (int i = 0; i < limit; i++) {
                String alert = alerts.get(i);
                Label label = new Label(alert);
                label.setWrapText(true);
                label.setFont(Font.font("Inter", FontWeight.BOLD, 11));
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
        List<com.kepo.model.Distribution> dists = controller.getDistributions();
        if (dists.isEmpty()) {
            Label label = new Label("Tidak ada pengiriman logistik bantuan.");
            label.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
            label.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
            distributionContainer.getChildren().add(label);
        } else {
            int limit = Math.min(dists.size(), 4);
            for (int i = 0; i < limit; i++) {
                com.kepo.model.Distribution d = dists.get(i);
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(2, 0, 2, 0));
                
                Label docLbl = new Label(d.getDocNum());
                docLbl.setFont(Font.font("Inter", FontWeight.BOLD, 12));
                docLbl.setTextFill(Color.web(ThemeConstants.PRIMARY));
                docLbl.setPrefWidth(120);

                Label shelterLbl = new Label(d.getShelterName());
                shelterLbl.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
                shelterLbl.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
                shelterLbl.setPrefWidth(120);

                Label detailLbl = new Label(d.getItemType() + " (" + d.getQuantity() + ")");
                detailLbl.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
                detailLbl.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
                detailLbl.setPrefWidth(100);

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
        int limit = Math.min(suggestions.size(), 4);
        for (int i = 0; i < limit; i++) {
            String suggestion = suggestions.get(i);
            HBox row = new HBox(6);
            row.setAlignment(Pos.TOP_LEFT);
            Label dot = new Label("•");
            dot.setTextFill(Color.web(ThemeConstants.PRIMARY_LIGHT));
            Label text = new Label(suggestion);
            text.setWrapText(true);
            text.setFont(Font.font("Inter", FontWeight.BOLD, 11));
            text.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
            HBox.setHgrow(text, Priority.ALWAYS);
            row.getChildren().addAll(dot, text);
            aiRecContainer.getChildren().add(row);
        }
    }
}
