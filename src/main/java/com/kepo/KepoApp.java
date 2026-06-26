package com.kepo;

import com.kepo.config.AppConfig;
import com.kepo.config.DatabaseConfig;
import com.kepo.controller.DashboardController;
import com.kepo.controller.InventoryController;
import com.kepo.controller.LoginController;
import com.kepo.controller.RefugeeShelterController;
import com.kepo.repository.*;
import com.kepo.service.*;
import com.kepo.service.ai.AIProvider;
import com.kepo.service.ai.AIProviderChain;
import com.kepo.service.ai.GeminiProvider;
import com.kepo.service.ai.LocalRuleBasedProvider;
import com.kepo.api.RestApiServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class KepoApp {

    private AppConfig appConfig;
    private DatabaseConfig dbConfig;

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

    private LoginController loginController;
    private DashboardController dashboardController;
    private InventoryController inventoryController;
    private RefugeeShelterController refugeeShelterController;

    private RestApiServer apiServer;
    private ObjectMapper objectMapper;

    public KepoApp() {
        try {
            appConfig = new AppConfig();
            dbConfig = DatabaseConfig.getInstance();
            dbConfig.initialize(appConfig);

            if (!dbConfig.testConnection()) {
                System.err.println("Database connection failed. Exiting.");
                System.exit(1);
            }

            dbConfig.runSchema();
            dbConfig.runSeed();

            UserRepository userRepo = new UserRepository(dbConfig);
            EventRepository eventRepo = new EventRepository(dbConfig);
            ShelterRepository shelterRepo = new ShelterRepository(dbConfig);
            RefugeeRepository refugeeRepo = new RefugeeRepository(dbConfig);
            MedicineRepository medicineRepo = new MedicineRepository(dbConfig);
            DistributionRepository distributionRepo = new DistributionRepository(dbConfig);
            DonorRepository donorRepo = new DonorRepository(dbConfig);
            AuditLogRepository auditRepo = new AuditLogRepository(dbConfig);
            InventoryTransactionRepository transactionRepo = new InventoryTransactionRepository(dbConfig);
            RefugeeMovementRepository movementRepo = new RefugeeMovementRepository(dbConfig);
            ShelterStockRepository shelterStockRepo = new ShelterStockRepository(dbConfig);
            SupplierRepository supplierRepo = new SupplierRepository(dbConfig);
            MedicineRequestRepository medicineRequestRepo = new MedicineRequestRepository(dbConfig);

            userService = new UserService(userRepo, auditRepo);
            eventService = new EventService(eventRepo, userService);
            shelterService = new ShelterService(shelterRepo, userService);
            refugeeService = new RefugeeService(refugeeRepo, shelterService, userService);
            inventoryService = new InventoryService(medicineRepo, transactionRepo, userService);
            distributionService = new DistributionService(distributionRepo, userService);
            donorService = new DonorService(donorRepo, userService);
            barcodeService = new BarcodeService(medicineRepo);
            reportService = new ReportService(shelterRepo, refugeeRepo, medicineRepo, distributionRepo, donorRepo, eventRepo, appConfig.getReportsOutputDir());

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

            loginController = new LoginController(userService);
            RefugeeShelterService refugeeShelterService = new RefugeeShelterService(refugeeRepo, movementRepo, shelterRepo);
            ShelterStockService shelterStockService = new ShelterStockService(shelterStockRepo, shelterRepo);
            refugeeShelterController = new RefugeeShelterController(refugeeShelterService, shelterStockService, userService);
            dashboardController = new DashboardController(shelterService, refugeeService, inventoryService, distributionService, eventService, aiRecommendationService, userService, shelterStockService);
            inventoryController = new InventoryController(inventoryService, barcodeService);

            SupplierService supplierService = new SupplierService(supplierRepo, userService);

            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.findAndRegisterModules();

            int port = Integer.parseInt(appConfig.get("server.port", "8080"));
            apiServer = new RestApiServer(
                port, objectMapper,
                userService, eventService, shelterService, refugeeService,
                inventoryService, distributionService, donorService,
                supplierService, reportService, barcodeService,
                aiRecommendationService, shelterStockService,
                loginController, dashboardController, inventoryController,
                refugeeShelterController,
                medicineRepo, shelterRepo, medicineRequestRepo
            );
            apiServer.start();
            System.out.println("KEPO API Server started on port " + port);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new KepoApp();
    }
}
