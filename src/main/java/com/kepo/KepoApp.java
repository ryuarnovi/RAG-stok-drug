package com.kepo;

import com.kepo.config.AppConfig;
import com.kepo.config.DatabaseConfig;
import com.kepo.controller.DashboardController;
import com.kepo.controller.InventoryController;
import com.kepo.controller.LoginController;
import com.kepo.repository.*;
import com.kepo.service.*;
import com.kepo.service.ai.AIProvider;
import com.kepo.service.ai.AIProviderChain;
import com.kepo.service.ai.GeminiProvider;
import com.kepo.service.ai.LocalRuleBasedProvider;
import com.kepo.view.LoginView;
import com.kepo.view.MainLayout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class KepoApp extends Application {

    private Stage primaryStage;
    private AppConfig appConfig;
    private DatabaseConfig dbConfig;

    // Services
    private UserService userService;
    private EventService eventService;
    private ShelterService shelterService;
    private RefugeeService refugeeService;
    private InventoryService inventoryService;
    private DistributionService distributionService;
    private DonorService donorService;
    private ReportService reportService;
    private BarcodeService barcodeService;
    private AIRecommendationService aiRecommendationService;

    // Controllers
    private LoginController loginController;
    private DashboardController dashboardController;
    private InventoryController inventoryController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("KEPO - Kendali Evakuasi Bencana");

        try {
            // 1. Initialize configurations
            appConfig = new AppConfig();
            dbConfig = DatabaseConfig.getInstance();
            dbConfig.initialize(appConfig);

            // 2. Test database connection
            if (!dbConfig.testConnection()) {
                showDatabaseErrorAlert();
                Platform.exit();
                System.exit(1);
            }

            // 3. Initialize schema and seed data
            dbConfig.runSchema();
            dbConfig.runSeed();

            // 4. Initialize repositories
            UserRepository userRepo = new UserRepository(dbConfig);
            EventRepository eventRepo = new EventRepository(dbConfig);
            ShelterRepository shelterRepo = new ShelterRepository(dbConfig);
            RefugeeRepository refugeeRepo = new RefugeeRepository(dbConfig);
            MedicineRepository medicineRepo = new MedicineRepository(dbConfig);
            DistributionRepository distributionRepo = new DistributionRepository(dbConfig);
            DonorRepository donorRepo = new DonorRepository(dbConfig);
            AuditLogRepository auditRepo = new AuditLogRepository(dbConfig);
            InventoryTransactionRepository transactionRepo = new InventoryTransactionRepository(dbConfig);

            // 5. Initialize services
            userService = new UserService(userRepo, auditRepo);
            eventService = new EventService(eventRepo, userService);
            shelterService = new ShelterService(shelterRepo, userService);
            refugeeService = new RefugeeService(refugeeRepo, shelterService, userService);
            inventoryService = new InventoryService(medicineRepo, transactionRepo, userService);
            distributionService = new DistributionService(distributionRepo, userService);
            donorService = new DonorService(donorRepo, userService);
            barcodeService = new BarcodeService(medicineRepo);
            reportService = new ReportService(shelterRepo, refugeeRepo, medicineRepo, distributionRepo, donorRepo, eventRepo, appConfig.getReportsOutputDir());

            // AI Initialization
            String provider = appConfig.getAIProvider().toUpperCase();
            AIProvider localFallback = new LocalRuleBasedProvider(shelterRepo, refugeeRepo, medicineRepo);
            AIProvider aiProvider;
            if ("GEMINI".equals(provider)) {
                String geminiKey = appConfig.get("ai.gemini.api_key");
                String geminiModel = appConfig.get("ai.gemini.model");
                aiProvider = new AIProviderChain(new GeminiProvider(geminiKey, geminiModel), localFallback);
            } else {
                aiProvider = localFallback;
            }

            aiRecommendationService = new AIRecommendationService(shelterRepo, refugeeRepo, medicineRepo, distributionRepo, eventRepo, aiProvider);

            // 6. Initialize controllers
            loginController = new LoginController(userService);
            dashboardController = new DashboardController(shelterService, refugeeService, inventoryService, distributionService, eventService, aiRecommendationService, userService);
            inventoryController = new InventoryController(inventoryService, barcodeService);

            // 7. Show Login view
            showLoginView();

        } catch (Exception e) {
            e.printStackTrace();
            showFatalErrorAlert(e.getMessage());
            Platform.exit();
            System.exit(1);
        }
    }

    public void showLoginView() {
        LoginView loginView = new LoginView(loginController, this);
        Scene scene = new Scene(loginView, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void showMainApplication() {
        MainLayout mainLayout = new MainLayout(
                dashboardController,
                inventoryController,
                userService,
                eventService,
                shelterService,
                refugeeService,
                distributionService,
                donorService,
                reportService,
                aiRecommendationService,
                this
        );
        Scene scene = new Scene(mainLayout, 1280, 800);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showDatabaseErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Koneksi Database Gagal");
        alert.setContentText("Aplikasi KEPO tidak dapat terhubung ke PostgreSQL.\n" +
                "Pastikan server PostgreSQL sudah berjalan dan konfigurasi DB_URL di file .env sudah benar.");
        alert.showAndWait();
    }

    private void showFatalErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fatal Error");
        alert.setHeaderText("Gagal Menjalankan Aplikasi");
        alert.setContentText("Terjadi kesalahan sistem: " + message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (dbConfig != null) {
            dbConfig.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
