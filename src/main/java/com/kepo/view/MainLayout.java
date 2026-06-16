package com.kepo.view;

import com.kepo.KepoApp;
import com.kepo.config.DatabaseConfig;
import com.kepo.controller.DashboardController;
import com.kepo.controller.InventoryController;
import com.kepo.controller.RefugeeShelterController;
import com.kepo.model.User;
import com.kepo.repository.SupplierRepository;
import com.kepo.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

public class MainLayout extends BorderPane {

    private final DashboardController dashboardController;
    private final InventoryController inventoryController;
    private final UserService userService;
    private final EventService eventService;
    private final ShelterService shelterService;
    private final RefugeeService refugeeService;
    private final DistributionService distributionService;
    private final DonorService donorService;
    private final ReportService reportService;
    private final AIRecommendationService aiRecService;
    private final RefugeeShelterController refugeeShelterController;
    private final KepoApp app;

    private VBox sidebarContainer;
    private StackPane contentArea;
    private Map<String, Node> panelsMap = new HashMap<>();
    private Map<String, Button> sidebarButtons = new HashMap<>();
    
    // Collapsible group tracking maps
    private Map<VBox, Button> categoryHeaders = new HashMap<>();
    private Map<String, VBox> itemCategoryBoxes = new HashMap<>();

    public MainLayout(DashboardController dashboardController, InventoryController inventoryController,
                      RefugeeShelterController refugeeShelterController,
                      UserService userService, EventService eventService, ShelterService shelterService,
                      RefugeeService refugeeService, DistributionService distributionService,
                      DonorService donorService, ReportService reportService,
                      AIRecommendationService aiRecService, KepoApp app) {
        this.dashboardController = dashboardController;
        this.inventoryController = inventoryController;
        this.refugeeShelterController = refugeeShelterController;
        this.userService = userService;
        this.eventService = eventService;
        this.shelterService = shelterService;
        this.refugeeService = refugeeService;
        this.distributionService = distributionService;
        this.donorService = donorService;
        this.reportService = reportService;
        this.aiRecService = aiRecService;
        this.app = app;

        initUI();
    }

    private void initUI() {
        // --- Top Header ---
        HBox header = new HBox();
        header.setPadding(new Insets(12, 24, 12, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + ThemeConstants.SURFACE + "; -fx-border-color: " + ThemeConstants.BORDER + "; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("KEPO COMMAND CENTER");
        titleLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-weight: 900; -fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        User currentUser = userService.getCurrentUser();
        String name = currentUser != null ? currentUser.getFullName() : "User";
        String role = currentUser != null ? currentUser.getRole().name() : "STAFF";

        Label userLabel = new Label("Selamat Bekerja, " + name + " (" + role + ")");
        userLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-family: 'Inter'; -fx-font-weight: bold; -fx-font-size: 13px;");

        Button logoutBtn = new Button("Keluar");
        logoutBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + "-fx-padding: 4 12 4 12;");
        logoutBtn.setOnAction(e -> handleLogout());

        HBox userInfoBox = new HBox(15, userLabel, logoutBtn);
        userInfoBox.setAlignment(Pos.CENTER);

        header.getChildren().addAll(titleLabel, spacer, userInfoBox);
        setTop(header);

        // --- Left Sidebar ---
        sidebarContainer = new VBox(5);
        sidebarContainer.setPadding(new Insets(15, 10, 15, 10));
        sidebarContainer.setPrefWidth(210);
        sidebarContainer.setStyle("-fx-background-color: " + ThemeConstants.SURFACE + ";");

        buildSidebarMenu();

        ScrollPane sidebarScroll = new ScrollPane(sidebarContainer);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setStyle("-fx-background-color: " + ThemeConstants.SURFACE + ";" +
                               "-fx-background: " + ThemeConstants.SURFACE + ";" +
                               "-fx-border-color: " + ThemeConstants.BORDER + ";" +
                               "-fx-border-width: 0 1 0 0;" +
                               "-fx-padding: 0;");
        setLeft(sidebarScroll);

        // --- Central Content Area ---
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND + ";");
        setCenter(contentArea);

        // --- Bottom Status Bar ---
        setBottom(createStatusBar());

        // Load Default Panel
        switchPanel("dashboard");
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(6, 20, 6, 20));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: " + ThemeConstants.SURFACE + "; -fx-border-color: " + ThemeConstants.BORDER + "; -fx-border-width: 1 0 0 0;");

        User currentUser = userService.getCurrentUser();
        String name = currentUser != null ? currentUser.getFullName() : "Operator";
        String role = currentUser != null ? currentUser.getRole().name() : "STAFF";
        Label opLabel = new Label("Petugas: " + name + " (" + role + ")");
        opLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-family: 'Inter'; -fx-font-weight: bold; -fx-font-size: 11px;");

        long activeEvents = eventService.getAllEvents().stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        Label eventLabel = new Label("Kejadian Bencana Aktif: " + activeEvents);
        eventLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-family: 'Inter'; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label dbLabel = new Label("Database: TERHUBUNG (HikariPool)");
        dbLabel.setStyle("-fx-text-fill: " + ThemeConstants.SECONDARY + "; -fx-font-family: 'Inter'; -fx-font-weight: bold; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label("Sistem Komando KEPO v2.0.0");
        timeLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-family: 'Inter'; -fx-font-size: 11px;");

        statusBar.getChildren().addAll(opLabel, eventLabel, dbLabel, spacer, timeLabel);
        return statusBar;
    }

    private void buildSidebarMenu() {
        sidebarContainer.getChildren().clear();
        categoryHeaders.clear();
        itemCategoryBoxes.clear();

        // Title brand (Linear style)
        Label brandLabel = new Label("KEPO");
        brandLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-weight: 900; -fx-font-size: 22px; -fx-padding: 0 15 15 15;");
        sidebarContainer.getChildren().add(brandLabel);

        // Standalone Dashboard button
        addSidebarButton("dashboard", "Dashboard");

        User currentUser = userService.getCurrentUser();
        User.Role role = currentUser != null ? currentUser.getRole() : User.Role.SHELTER_OFFICER;

        // Collapsible Categories
        if (role == User.Role.ADMIN || role == User.Role.SHELTER_OFFICER) {
            addCollapsibleCategory("OPERASI", 
                    new String[]{"event", "shelter", "refugee"}, 
                    new String[]{"Event Bencana", "Shelter", "Pengungsi"}
            );
        }

        if (role == User.Role.ADMIN || role == User.Role.HEALTH_OFFICER) {
            addCollapsibleCategory("LOGISTIK", 
                    new String[]{"medicine", "distribution", "supp_donor"}, 
                    new String[]{"Inventaris", "Distribusi", "Supplier"}
            );
        }

        addCollapsibleCategory("ANALISIS", 
                new String[]{"ai", "prediction"}, 
                new String[]{"AI Assistant", "Prediksi"}
        );

        addCollapsibleCategory("LAPORAN", 
                new String[]{"report"}, 
                new String[]{"Report Center"}
        );

        addCollapsibleCategory("SYSTEM", 
                new String[]{"settings"}, 
                new String[]{"Pengaturan"}
        );
    }

    private void addCollapsibleCategory(String categoryName, String[] panelIds, String[] panelLabels) {
        // Header Button with dropdown caret toggle
        Button headerBtn = new Button(categoryName + "   v");
        headerBtn.setMaxWidth(Double.MAX_VALUE);
        headerBtn.setAlignment(Pos.CENTER_LEFT);
        headerBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + ";" +
                "-fx-font-family: 'Inter';" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 12 15 4 15;" +
                "-fx-cursor: hand;");

        VBox itemsBox = new VBox(5);
        itemsBox.setPadding(new Insets(0, 0, 0, 10)); // slight indent for submenus

        for (int i = 0; i < panelIds.length; i++) {
            String id = panelIds[i];
            String label = panelLabels[i];
            
            Button btn = new Button(label);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setStyle(getSidebarButtonInactiveStyle());
            btn.setOnAction(e -> switchPanel(id));

            itemsBox.getChildren().add(btn);
            sidebarButtons.put(id, btn);
            itemCategoryBoxes.put(id, itemsBox);
        }

        categoryHeaders.put(itemsBox, headerBtn);

        headerBtn.setOnAction(e -> {
            boolean visible = !itemsBox.isVisible();
            itemsBox.setVisible(visible);
            itemsBox.setManaged(visible);
            headerBtn.setText(categoryName + (visible ? "   v" : "   >"));
        });

        sidebarContainer.getChildren().addAll(headerBtn, itemsBox);
    }

    private void addSidebarButton(String id, String label) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(getSidebarButtonInactiveStyle());
        btn.setOnAction(e -> switchPanel(id));

        sidebarContainer.getChildren().add(btn);
        sidebarButtons.put(id, btn);
    }

    public void switchPanel(String id) {
        // Auto-expand parent category if collapsed
        VBox parentBox = itemCategoryBoxes.get(id);
        if (parentBox != null) {
            parentBox.setVisible(true);
            parentBox.setManaged(true);
            Button headerBtn = categoryHeaders.get(parentBox);
            if (headerBtn != null) {
                String currentText = headerBtn.getText();
                if (currentText.endsWith("   >")) {
                    String catName = currentText.substring(0, currentText.length() - 4);
                    headerBtn.setText(catName + "   v");
                }
            }
        }

        // Update sidebar buttons styling
        for (Map.Entry<String, Button> entry : sidebarButtons.entrySet()) {
            if (entry.getKey().equals(id)) {
                entry.getValue().setStyle(getSidebarButtonActiveStyle());
            } else {
                entry.getValue().setStyle(getSidebarButtonInactiveStyle());
            }
        }

        // Lazy load views
        Node targetPanel = panelsMap.get(id);
        if (targetPanel == null) {
            targetPanel = createPanel(id);
            if (targetPanel != null) {
                panelsMap.put(id, targetPanel);
            }
        }

        if (targetPanel != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(targetPanel);
            
            // Refresh data in view if it supports refreshing
            if (targetPanel instanceof RefreshablePanel) {
                ((RefreshablePanel) targetPanel).refreshData();
            }
        }
    }

    private Node createPanel(String id) {
        return switch (id) {
            case "dashboard" -> new DashboardView(dashboardController, this);
            case "event" -> new EventPanel(eventService, this);
            case "shelter" -> new ShelterPanel(shelterService, this);
            case "refugee" -> new RefugeePanel(refugeeService, shelterService, refugeeShelterController, this);
            case "medicine" -> new MedicinePanel(inventoryController, supplierService(), this);
            case "distribution" -> new DistributionPanel(distributionService, shelterService, inventoryController, refugeeShelterController, this);
            case "supp_donor" -> new SupplierDonorPanel(supplierService(), donorService, this);
            case "ai" -> new AIChatPanel(aiRecService, this);
            case "prediction" -> new PredictionPanel(aiRecService, this);
            case "report" -> new ReportPanel(reportService, this);
            case "settings" -> new SettingsPanel(userService, this);
            default -> null;
        };
    }

    private void handleLogout() {
        userService.logout();
        app.showLoginView();
    }

    // Lazy helpers to instantiate repo dependencies inside layout
    private SupplierService supplierService() {
        DatabaseConfig db = DatabaseConfig.getInstance();
        return new SupplierService(new SupplierRepository(db), userService);
    }

    private String getSidebarButtonInactiveStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + ";" +
                "-fx-font-family: 'Inter';" +
                "-fx-font-weight: normal;" +
                "-fx-background-radius: 12;" +
                "-fx-alignment: center-left;" +
                "-fx-padding: 8 15 8 15;" +
                "-fx-cursor: hand;";
    }

    private String getSidebarButtonActiveStyle() {
        return "-fx-background-color: #f0f9ff;" +
                "-fx-text-fill: " + ThemeConstants.PRIMARY_LIGHT + ";" +
                "-fx-font-family: 'Inter';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + ThemeConstants.PRIMARY_LIGHT + ";" +
                "-fx-border-width: 0 0 0 4;" +
                "-fx-background-radius: 0 12 12 0;" +
                "-fx-alignment: center-left;" +
                "-fx-padding: 8 15 8 11;" +
                "-fx-cursor: hand;";
    }
}
