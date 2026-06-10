package com.pharmastock.view;

import com.pharmastock.controller.*;
import com.pharmastock.view.ai.AIChatPanel;
import com.pharmastock.view.components.SidebarNav;
import com.pharmastock.view.dashboard.DashboardPanel;
import com.pharmastock.view.inventory.InventoryPanel;
import com.pharmastock.view.profile.ProfilePanel;
import com.pharmastock.view.report.ReportPanel;
import com.pharmastock.view.supplier.SupplierPanel;
import com.pharmastock.service.BarcodeService;
import com.pharmastock.service.ReportService;
import com.pharmastock.service.NotificationService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarNav sidebarNav;
    private Runnable onLogout;

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    private final DashboardController dashboardController;
    private final InventoryController inventoryController;
    private final SupplierController supplierController;
    private final AIController aiController;
    private final LoginController loginController;
    private final BarcodeService barcodeService;
    private final ReportService reportService;
    private final NotificationService notificationService;

    public MainFrame(DashboardController dashboardController,
            InventoryController inventoryController,
            SupplierController supplierController,
            AIController aiController,
            LoginController loginController,
            BarcodeService barcodeService,
            ReportService reportService,
            NotificationService notificationService) {
        this.dashboardController = dashboardController;
        this.inventoryController = inventoryController;
        this.supplierController = supplierController;
        this.aiController = aiController;
        this.loginController = loginController;
        this.barcodeService = barcodeService;
        this.reportService = reportService;
        this.notificationService = notificationService;

        setTitle("PharmaStock - Pharmacy Inventory Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeConstants.BACKGROUND);

        buildUI();
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void showAddMedicineDialog() {
        com.pharmastock.view.inventory.AddMedicineDialog dialog = new com.pharmastock.view.inventory.AddMedicineDialog(
                this, inventoryController);
        dialog.setVisible(true);
        // Refresh visible panels
        for (Component c : contentPanel.getComponents()) {
            if (c.isVisible() && c instanceof DashboardPanel dp) {
                dp.refreshData();
            } else if (c.isVisible() && c instanceof InventoryPanel ip) {
                ip.refreshData();
            }
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Sidebar
        sidebarNav = new SidebarNav();
        sidebarNav.setOnNavigate(this::navigateTo);
        add(sidebarNav, BorderLayout.WEST);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeConstants.BACKGROUND);

        // Build panels
        DashboardPanel dashboardPanel = new DashboardPanel(dashboardController, notificationService);
        InventoryPanel inventoryPanel = new InventoryPanel(inventoryController, notificationService);
        SupplierPanel supplierPanel = new SupplierPanel(supplierController);
        AIChatPanel aiChatPanel = new AIChatPanel(aiController);
        ReportPanel reportPanel = new ReportPanel(reportService);
        ProfilePanel profilePanel = new ProfilePanel(loginController);

        // Scan panel (simplified - uses barcode service)
        JPanel scanPanel = buildScanPanel();

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(scanPanel, "scan");
        contentPanel.add(aiChatPanel, "ai");
        contentPanel.add(inventoryPanel, "inventory");
        contentPanel.add(supplierPanel, "supplier");
        contentPanel.add(reportPanel, "report");
        contentPanel.add(profilePanel, "profile");

        add(contentPanel, BorderLayout.CENTER);

        // Default to dashboard
        sidebarNav.setActive("dashboard");
        cardLayout.show(contentPanel, "dashboard");
    }

    public void triggerLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            loginController.logout();
            if (onLogout != null) {
                onLogout.run();
            }
        }
    }

    private void navigateTo(String key) {
        if ("logout".equals(key)) {
            triggerLogout();
            return;
        }

        cardLayout.show(contentPanel, key);

        // Refresh data when navigating
        for (Component c : contentPanel.getComponents()) {
            if (c.isVisible() && c instanceof DashboardPanel dp) {
                dp.refreshData();
            } else if (c.isVisible() && c instanceof InventoryPanel ip) {
                ip.refreshData();
            } else if (c.isVisible() && c instanceof SupplierPanel sp) {
                sp.refreshData();
            }
        }
    }

    private JPanel buildScanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeConstants.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Scan Barcode");
        title.setFont(ThemeConstants.fontHeadlineLg());
        title.setForeground(ThemeConstants.ON_SURFACE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Masukkan kode barcode obat untuk pencarian cepat");
        desc.setFont(ThemeConstants.fontBodyMd());
        desc.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField barcodeField = new JTextField();
        barcodeField.setFont(ThemeConstants.fontTitleMd());
        barcodeField.setMaximumSize(new Dimension(400, ThemeConstants.TOUCH_TARGET));
        barcodeField.setAlignmentX(Component.CENTER_ALIGNMENT);
        barcodeField.putClientProperty("JTextField.placeholderText", "Masukkan atau scan kode obat...");

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(ThemeConstants.fontBodyMd());
        resultLabel.setForeground(ThemeConstants.ON_SURFACE);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton searchBtn = new JButton("Cari");
        searchBtn.setFont(ThemeConstants.fontTitleMd());
        searchBtn.putClientProperty("FlatLaf.styleClass", "primary");
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchBtn.setMaximumSize(new Dimension(200, ThemeConstants.TOUCH_TARGET));

        // Barcode image display
        JLabel barcodeImage = new JLabel();
        barcodeImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchBtn.addActionListener(e -> {
            String code = barcodeField.getText().trim();
            if (!code.isEmpty()) {
                var med = barcodeService.lookupMedicine(code);
                if (med.isPresent()) {
                    var m = med.get();
                    resultLabel.setText(String.format(
                            "<html><center>Ditemukan: <b>%s</b><br>Stok: %d %s | Status: %s</center></html>",
                            m.getMedicineName(), m.getStockQuantity(), m.getUnit(), m.getStockStatusLabel()));
                    resultLabel.setForeground(ThemeConstants.SECONDARY);

                    var img = barcodeService.generateBarcode(code);
                    if (img != null) {
                        barcodeImage.setIcon(new ImageIcon(img));
                    }
                } else {
                    resultLabel.setText("Obat dengan kode '" + code + "' tidak ditemukan.");
                    resultLabel.setForeground(ThemeConstants.DANGER);
                    barcodeImage.setIcon(null);
                }
            }
        });

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(desc);
        centerPanel.add(Box.createVerticalStrut(32));
        centerPanel.add(barcodeField);
        centerPanel.add(Box.createVerticalStrut(16));
        centerPanel.add(searchBtn);
        centerPanel.add(Box.createVerticalStrut(24));
        centerPanel.add(resultLabel);
        centerPanel.add(Box.createVerticalStrut(16));
        centerPanel.add(barcodeImage);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }
}
