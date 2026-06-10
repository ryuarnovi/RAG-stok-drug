package com.pharmastock;

import com.pharmastock.config.AppConfig;
import com.pharmastock.config.DatabaseConfig;
import com.pharmastock.controller.*;
import com.pharmastock.repository.*;
import com.pharmastock.service.*;
import com.pharmastock.service.ai.AIProvider;
import com.pharmastock.service.ai.AIProviderChain;
import com.pharmastock.service.ai.GeminiProvider;
import com.pharmastock.service.ai.LocalRuleBasedProvider;
import com.pharmastock.service.ai.OpenAIProvider;
import com.pharmastock.view.LoginView;
import com.pharmastock.view.MainFrame;
import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;

/**
 * PharmaStock Application Entry Point.
 * Manual dependency injection dan bootstrap seluruh layer.
 */
public class PharmaStockApp {

    public static void main(String[] args) {
        // Set Look and Feel to FlatLaf Light for a modern, premium look and feel
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            
            // Custom design token styling for FlatLaf components
            UIManager.put("Button.arc", ThemeConstants.RADIUS_LG);
            UIManager.put("Component.arc", ThemeConstants.RADIUS_LG);
            UIManager.put("TextComponent.arc", ThemeConstants.RADIUS_LG);
            
            // Make button background painting behave correctly
            UIManager.put("Button.opaque", true);
            UIManager.put("Button.paintBackground", true);
            
            // Customize colors to fit ThemeConstants
            UIManager.put("Button.background", ThemeConstants.SURFACE);
            UIManager.put("Button.foreground", ThemeConstants.ON_SURFACE);
            UIManager.put("Button.hoverBackground", ThemeConstants.SURFACE_CONTAINER_LOW);
            UIManager.put("Button.focusedBackground", ThemeConstants.SURFACE_CONTAINER);

            // Global FlatLaf custom button style classes
            UIManager.put("[style]Button.primary", "background: #006591; foreground: #ffffff; hoverBackground: #004b6d; pressedBackground: #003751; focusedBackground: #004b6d; borderWidth: 0; margin: 4,12,4,12; arc: " + ThemeConstants.RADIUS_LG);
            UIManager.put("[style]Button.secondary", "background: #006C49; foreground: #ffffff; hoverBackground: #004e35; pressedBackground: #003723; focusedBackground: #004e35; borderWidth: 0; margin: 4,12,4,12; arc: " + ThemeConstants.RADIUS_LG);
            UIManager.put("[style]Button.danger", "background: #EF4444; foreground: #ffffff; hoverBackground: #dc2626; pressedBackground: #b91c1c; focusedBackground: #dc2626; borderWidth: 0; margin: 4,12,4,12; arc: " + ThemeConstants.RADIUS_LG);
            UIManager.put("[style]Button.success", "background: #006C49; foreground: #ffffff; hoverBackground: #004e35; pressedBackground: #003723; focusedBackground: #004e35; borderWidth: 0; margin: 4,12,4,12; arc: " + ThemeConstants.RADIUS_LG);
            UIManager.put("[style]Button.outline", "background: #ffffff; foreground: #111C2D; borderWidth: 1; borderColor: #E2E8F0; hoverBackground: #F0F3FF; pressedBackground: #E7EEFF; arc: " + ThemeConstants.RADIUS_LG);
            
            // Table adjustments
            UIManager.put("Table.selectionBackground", ThemeConstants.PRIMARY_TINT_10);
            UIManager.put("Table.selectionForeground", ThemeConstants.ON_SURFACE);
            UIManager.put("TableHeader.background", ThemeConstants.SURFACE_CONTAINER_LOW);
            UIManager.put("TableHeader.foreground", ThemeConstants.ON_SURFACE);
            
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // fallback
            }
        }

        SwingUtilities.invokeLater(() -> {
            try {
                AppConfig appConfig = new AppConfig();

                DatabaseConfig dbConfig = DatabaseConfig.getInstance();
                dbConfig.initialize(appConfig);

                if (!dbConfig.testConnection()) {
                    JOptionPane.showMessageDialog(null,
                            "Tidak dapat terhubung ke database.\n" +
                                    "Pastikan MySQL berjalan dan konfigurasi DB_URL di file .env benar.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                dbConfig.runSchema();
                dbConfig.runSeed();

                IUserRepository userRepo = new UserRepository(dbConfig);
                IMedicineRepository medicineRepo = new MedicineRepository(dbConfig);
                ISupplierRepository supplierRepo = new SupplierRepository(dbConfig);
                IInventoryTransactionRepository transactionRepo = new InventoryTransactionRepository(dbConfig);

                AIProvider aiProvider = createAIProvider(appConfig, medicineRepo);
                System.err.println("[PharmaStock] AI Provider: " + appConfig.getAIProvider().toUpperCase()
                        + " (Gemini model: " + appConfig.get("ai.gemini.model", "N/A")
                        + ", API key configured: " + !appConfig.get("ai.gemini.api_key", "").isEmpty() + ")");

                MedicalKnowledgeService knowledgeService = new MedicalKnowledgeService(medicineRepo);
                InventoryService inventoryService = new InventoryService(medicineRepo, transactionRepo);
                SupplierService supplierService = new SupplierService(supplierRepo);
                BarcodeService barcodeService = new BarcodeService(medicineRepo);
                ReportService reportService = new ReportService(dbConfig, appConfig.getReportsOutputDir());
                AIRecommendationService aiService = new AIRecommendationService(medicineRepo, transactionRepo,
                        aiProvider, knowledgeService);
                NotificationService notificationService = new NotificationServiceImpl(medicineRepo);

                LoginController loginController = new LoginController(userRepo);
                DashboardController dashboardController = new DashboardController(inventoryService, aiService);
                InventoryController inventoryController = new InventoryController(inventoryService, barcodeService);
                SupplierController supplierController = new SupplierController(supplierService);
                AIController aiController = new AIController(aiService);

                // Start login and main application flow
                showLogin(loginController, dashboardController, inventoryController, 
                          supplierController, aiController, barcodeService, reportService, notificationService);

                // Shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(dbConfig::shutdown));

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Gagal menjalankan aplikasi: " + e.getMessage(),
                        "Fatal Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static AIProvider createAIProvider(AppConfig config, IMedicineRepository medicineRepo) {
        String provider = config.getAIProvider().toUpperCase();
        LocalRuleBasedProvider localFallback = new LocalRuleBasedProvider(medicineRepo);
        return switch (provider) {
            case "OPENAI" -> new AIProviderChain(
                    new OpenAIProvider(config.get("ai.openai.api_key"), config.get("ai.openai.model")),
                    localFallback);
            case "GEMINI" -> new AIProviderChain(
                    new GeminiProvider(config.get("ai.gemini.api_key"), config.get("ai.gemini.model")),
                    localFallback);
            default -> localFallback;
        };
    }

    private static void showLogin(
            LoginController loginController,
            DashboardController dashboardController,
            InventoryController inventoryController,
            SupplierController supplierController,
            AIController aiController,
            BarcodeService barcodeService,
            ReportService reportService,
            NotificationService notificationService) {
        
        JFrame loginFrame = new JFrame("PharmaStock - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(500, 550);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

        LoginView loginView = new LoginView(loginController);
        loginView.setOnLoginSuccess(() -> {
            loginFrame.dispose();

            // Show main application
            MainFrame mainFrame = new MainFrame(
                    dashboardController, inventoryController,
                    supplierController, aiController, loginController,
                    barcodeService, reportService, notificationService);
            
            mainFrame.setOnLogout(() -> {
                mainFrame.dispose();
                // Re-show login
                showLogin(loginController, dashboardController, inventoryController,
                        supplierController, aiController, barcodeService, reportService, notificationService);
            });
            
            mainFrame.setVisible(true);
        });

        loginFrame.setContentPane(loginView);
        loginFrame.setVisible(true);
    }
}
