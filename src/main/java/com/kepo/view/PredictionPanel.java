package com.kepo.view;

import com.kepo.service.AIRecommendationService;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class PredictionPanel extends ScrollPane implements RefreshablePanel {

    private final AIRecommendationService aiRecommendationService;
    private final MainLayout mainLayout;

    private VBox mainContainer;
    private Label summaryLabel;
    private VBox shelterForecastContainer;
    private VBox priorityContainer;
    private VBox lackingLogisticsContainer;
    private VBox medicineForecastContainer;

    private Button runBtn;
    private ProgressIndicator progressIndicator;

    public PredictionPanel(AIRecommendationService aiRecommendationService, MainLayout mainLayout) {
        this.aiRecommendationService = aiRecommendationService;
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

        // --- Header Row ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Analisis Prediktif & Proyeksi");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        runBtn = new Button("Jalankan Analisis");
        runBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        runBtn.setOnAction(e -> refreshData());

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        progressIndicator.setVisible(false);

        header.setSpacing(10);
        header.getChildren().addAll(title, spacer, progressIndicator, runBtn);
        mainContainer.getChildren().add(header);

        // --- Executive Summary Card ---
        VBox summaryCard = new VBox(10);
        summaryCard.setPadding(new Insets(16));
        summaryCard.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1 1 1 5; " +
            "-fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 " + ThemeConstants.PRIMARY + "; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(17,28,45,0.05), 5, 0, 0, 1);"
        );
        
        Label summaryTitle = new Label("Ringkasan Eksekutif Situasi");
        summaryTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        summaryTitle.setTextFill(Color.web(ThemeConstants.PRIMARY));

        summaryLabel = new Label("Sedang mengkalkulasi data lapangan...");
        summaryLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
        summaryLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
        summaryLabel.setWrapText(true);
        summaryLabel.setMinHeight(Region.USE_PREF_SIZE);
        summaryLabel.prefWidthProperty().bind(summaryCard.widthProperty().subtract(32));

        summaryCard.getChildren().addAll(summaryTitle, summaryLabel);
        mainContainer.getChildren().add(summaryCard);

        // --- Two Column Layout ---
        HBox columnsContainer = new HBox(20);
        columnsContainer.setAlignment(Pos.TOP_LEFT);
        
        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        leftColumn.prefWidthProperty().bind(columnsContainer.widthProperty().multiply(0.5).subtract(10));
        
        VBox rightColumn = new VBox(20);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        rightColumn.prefWidthProperty().bind(columnsContainer.widthProperty().multiply(0.5).subtract(10));

        // --- LEFT COLUMN CARDS ---

        // 1. Shelter Projections Card
        VBox shelterCard = new VBox(15);
        shelterCard.setPadding(new Insets(16));
        shelterCard.setStyle(ThemeConstants.CARD_STYLE);
        
        Label shelterTitle = new Label("Proyeksi Kapasitas Shelter");
        shelterTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        shelterTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        shelterForecastContainer = new VBox(10);
        shelterCard.getChildren().addAll(shelterTitle, shelterForecastContainer);
        leftColumn.getChildren().add(shelterCard);

        // 2. Priority Alerts Card
        VBox priorityCard = new VBox(15);
        priorityCard.setPadding(new Insets(16));
        priorityCard.setStyle(ThemeConstants.CARD_STYLE);

        Label priorityTitle = new Label("Prioritas Alokasi & Distribusi");
        priorityTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        priorityTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        priorityContainer = new VBox(10);
        priorityCard.getChildren().addAll(priorityTitle, priorityContainer);
        leftColumn.getChildren().add(priorityCard);

        // --- RIGHT COLUMN CARDS ---

        // 3. Needs Analysis Card (What is Lacking)
        VBox lackingCard = new VBox(15);
        lackingCard.setPadding(new Insets(16));
        lackingCard.setStyle(ThemeConstants.CARD_STYLE);

        Label lackingTitle = new Label("Analisis Kebutuhan Logistik (Apa yang Kurang)");
        lackingTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        lackingTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        lackingLogisticsContainer = new VBox(10);
        lackingCard.getChildren().addAll(lackingTitle, lackingLogisticsContainer);
        rightColumn.getChildren().add(lackingCard);

        // 4. Medicine Depletion Card
        VBox medicineCard = new VBox(15);
        medicineCard.setPadding(new Insets(16));
        medicineCard.setStyle(ThemeConstants.CARD_STYLE);

        Label medicineTitle = new Label("Proyeksi & Ketersediaan Stok Obat");
        medicineTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        medicineTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        medicineForecastContainer = new VBox(10);
        medicineCard.getChildren().addAll(medicineTitle, medicineForecastContainer);
        rightColumn.getChildren().add(medicineCard);

        columnsContainer.getChildren().addAll(leftColumn, rightColumn);
        mainContainer.getChildren().add(columnsContainer);

        // Initial load
        refreshData();
    }

    @Override
    public void refreshData() {
        if (runBtn != null) {
            runBtn.setDisable(true);
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        summaryLabel.setText("Mengkalkulasi data lapangan dan memproses proyeksi...");

        javafx.concurrent.Task<PredictionResult> task = new javafx.concurrent.Task<>() {
            @Override
            protected PredictionResult call() throws Exception {
                String summary = aiRecommendationService.getSituationalExecutiveSummary();
                List<String> shelterForecasts = aiRecommendationService.getShelterOccupancyPredictions();
                List<String> priorities = aiRecommendationService.getPriorityShelters();
                List<String> medPredictions = aiRecommendationService.getMedicineStockDepletionPredictions();
                List<String> lackingLogistics = aiRecommendationService.getLackingLogisticsAnalysis();
                
                return new PredictionResult(summary, shelterForecasts, priorities, medPredictions, lackingLogistics);
            }
        };

        task.setOnSucceeded(e -> {
            PredictionResult result = task.getValue();
            
            summaryLabel.setText(result.summary);

            // 1. Shelter Forecasts
            shelterForecastContainer.getChildren().clear();
            for (String sf : result.shelterForecasts) {
                String name = sf;
                String pctText = "";
                String status = "AMAN";
                String desc = "";
                double progress = -1.0;

                if (sf.contains(" - ")) {
                    String[] parts = sf.split(" - ", 2);
                    String left = parts[0].trim();
                    String right = parts[1].trim();
                    
                    if (left.contains(" (")) {
                        name = left.substring(0, left.indexOf(" (")).trim();
                        pctText = left.substring(left.indexOf(" (") + 2, left.indexOf(")")).trim();
                        if (pctText.contains("%")) {
                            try {
                                String cleanPct = pctText.replace("%", "").replace("Terisi", "").trim();
                                progress = Double.parseDouble(cleanPct) / 100.0;
                            } catch (Exception ignored) {}
                        }
                    } else {
                        name = left;
                    }
                    
                    if (right.contains("[") && right.contains("]")) {
                        status = right.substring(right.indexOf("[") + 1, right.indexOf("]")).trim();
                        desc = right.substring(right.indexOf("]") + 1).trim();
                    } else {
                        desc = right;
                    }
                } else {
                    if (sf.contains("[KRITIS]")) status = "KRITIS";
                    else if (sf.contains("[WASPADA]")) status = "WASPADA";
                    desc = sf;
                }

                Node rowCard = createRowCard(status, name, pctText.isEmpty() ? null : pctText, desc, progress);
                if (rowCard instanceof Region) {
                    ((Region) rowCard).prefWidthProperty().bind(shelterForecastContainer.widthProperty().subtract(10));
                }
                shelterForecastContainer.getChildren().add(rowCard);
            }

            // 2. Priority Distribution Alerts
            priorityContainer.getChildren().clear();
            for (String p : result.priorities) {
                String status = "PRIORITAS";
                String name = p;
                String desc = "";

                if (p.contains("] ")) {
                    status = p.substring(p.indexOf("[") + 1, p.indexOf("]")).trim();
                    String rightSide = p.substring(p.indexOf("] ") + 2).trim();
                    if (rightSide.contains(" - ")) {
                        String[] parts = rightSide.split(" - ", 2);
                        name = parts[0].trim();
                        desc = parts[1].trim();
                    } else {
                        name = rightSide;
                    }
                } else {
                    if (p.contains(" - ")) {
                        String[] parts = p.split(" - ", 2);
                        name = parts[0].trim();
                        desc = parts[1].trim();
                    }
                }

                Node rowCard = createRowCard(status, name, null, desc, -1.0);
                if (rowCard instanceof Region) {
                    ((Region) rowCard).prefWidthProperty().bind(priorityContainer.widthProperty().subtract(10));
                }
                priorityContainer.getChildren().add(rowCard);
            }

            // 3. Lacking Logistics Analysis ("Apa yang Kurang")
            lackingLogisticsContainer.getChildren().clear();
            for (String ll : result.lackingLogistics) {
                String status = "AMAN";
                String name = ll;
                String desc = "";

                if (ll.contains("] ")) {
                    status = ll.substring(ll.indexOf("[") + 1, ll.indexOf("]")).trim();
                    String rightSide = ll.substring(ll.indexOf("] ") + 2).trim();
                    if (rightSide.contains(" membutuhkan ")) {
                        String[] parts = rightSide.split(" membutuhkan ", 2);
                        name = parts[0].trim() + " - Kebutuhan Obat";
                        desc = "Dibutuhkan " + parts[1].trim();
                    } else {
                        name = rightSide;
                    }
                }

                Node rowCard = createRowCard(status, name, null, desc, -1.0);
                if (rowCard instanceof Region) {
                    ((Region) rowCard).prefWidthProperty().bind(lackingLogisticsContainer.widthProperty().subtract(10));
                }
                lackingLogisticsContainer.getChildren().add(rowCard);
            }

            // 4. Medicine Projections
            medicineForecastContainer.getChildren().clear();
            for (String mp : result.medPredictions) {
                String name = mp;
                String status = "STABIL";
                String desc = "";

                if (mp.contains(" - ")) {
                    String[] parts = mp.split(" - ", 2);
                    name = parts[0].trim();
                    String right = parts[1].trim();
                    if (right.contains("[") && right.contains("]")) {
                        status = right.substring(right.indexOf("[") + 1, right.indexOf("]")).trim();
                        desc = right.substring(right.indexOf("]") + 1).trim();
                    } else {
                        desc = right;
                    }
                } else {
                    if (mp.contains("[KRITIS]")) status = "KRITIS";
                    else if (mp.contains("[WASPADA]")) status = "WASPADA";
                    desc = mp;
                }

                Node medCard = createRowCard(status, name, null, desc, -1.0);
                if (medCard instanceof Region) {
                    ((Region) medCard).prefWidthProperty().bind(medicineForecastContainer.widthProperty().subtract(10));
                }
                medicineForecastContainer.getChildren().add(medCard);
            }

            if (runBtn != null) runBtn.setDisable(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            summaryLabel.setText("Gagal menjalankan analisis prediktif: " + (ex != null ? ex.getMessage() : "Kesalahan internal"));
            if (runBtn != null) runBtn.setDisable(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private javafx.scene.Node createRowCard(String status, String title, String subtitle, String desc, double progress) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        
        String colorHex = switch (status.toUpperCase()) {
            case "KRITIS", "PRIORITAS 1", "MENDESAK" -> "#ef4444";
            case "WASPADA", "PRIORITAS 2" -> "#f59e0b";
            case "AMAN", "STABIL" -> "#006c49";
            default -> "#3b82f6";
        };

        card.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1 1 1 5; " +
            "-fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 " + colorHex + "; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(17,28,45,0.03), 4, 0, 0, 1);"
        );
        
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        Label badgeLabel = new Label(status);
        String badgeStyle = switch (status.toUpperCase()) {
            case "KRITIS", "PRIORITAS 1", "MENDESAK" -> ThemeConstants.BADGE_CRITICAL;
            case "WASPADA", "PRIORITAS 2" -> ThemeConstants.BADGE_WARNING;
            case "AMAN", "STABIL" -> ThemeConstants.BADGE_SAFE;
            default -> ThemeConstants.BADGE_ACTIVE;
        };
        badgeLabel.setStyle(badgeStyle);
        
        topRow.getChildren().addAll(titleLabel, badgeLabel);
        card.getChildren().add(topRow);

        if (subtitle != null && !subtitle.isEmpty()) {
            Label subLabel = new Label(subtitle);
            subLabel.setFont(Font.font("Inter", FontWeight.BOLD, 11));
            subLabel.setTextFill(Color.web(ThemeConstants.PRIMARY));
            subLabel.setWrapText(true);
            subLabel.setMinHeight(Region.USE_PREF_SIZE);
            card.getChildren().add(subLabel);
        }

        if (progress >= 0.0) {
            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
            progressBar.setProgress(progress);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: #f1f5f9;");
            card.getChildren().add(progressBar);
        }

        if (desc != null && !desc.isEmpty()) {
            Label descLabel = new Label(desc);
            descLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
            descLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
            descLabel.setWrapText(true);
            descLabel.setMinHeight(Region.USE_PREF_SIZE);
            descLabel.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(descLabel);
        }
        
        return card;
    }

    private static class PredictionResult {
        final String summary;
        final List<String> shelterForecasts;
        final List<String> priorities;
        final List<String> medPredictions;
        final List<String> lackingLogistics;

        PredictionResult(String summary, List<String> shelterForecasts, List<String> priorities, List<String> medPredictions, List<String> lackingLogistics) {
            this.summary = summary;
            this.shelterForecasts = shelterForecasts;
            this.priorities = priorities;
            this.medPredictions = medPredictions;
            this.lackingLogistics = lackingLogistics;
        }
    }
}
