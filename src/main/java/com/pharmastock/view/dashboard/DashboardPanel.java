package com.pharmastock.view.dashboard;

import com.pharmastock.controller.DashboardController;
import com.pharmastock.model.InventoryTransaction;
import com.pharmastock.service.InventoryService;
import com.pharmastock.service.NotificationService;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.MainFrame;
import com.pharmastock.view.components.BasePanel;
import com.pharmastock.view.components.RoundedPanel;
import com.pharmastock.view.components.StatusBadge;
import com.pharmastock.view.components.NotificationPopup;
import com.pharmastock.view.components.ScrollablePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends BasePanel {

    private final DashboardController controller;
    private final NotificationService notificationService;
    
    private JPanel statsPanel;
    private DefaultTableModel activityTableModel;
    private JTable activityTable;
    private JLayeredPane layeredPane;
    private JPanel mainContentPanel;
    private JButton bellBtn;

    public DashboardPanel(DashboardController controller, NotificationService notificationService) {
        this.controller = controller;
        this.notificationService = notificationService;
        
        setBackground(ThemeConstants.BACKGROUND);
        setLayout(new BorderLayout());
        
        initUI();
        refreshData();
    }

    @Override
    protected void initUI() {
        // Use a JLayeredPane to allow overlaying the floating action button
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane, BorderLayout.CENTER);

        // Main content wrapper
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        ScrollablePanel scrollContent = new ScrollablePanel();
        scrollContent.setOpaque(false);
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));

        // Header (Pharmacy Dashboard + Profile Details)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Pharmacy Dashboard");
        title.setFont(ThemeConstants.fontHeadlineLg());
        title.setForeground(ThemeConstants.ON_SURFACE);

        JLabel subtitle = new JLabel("Real-time oversight of clinical stock and logistics.");
        subtitle.setFont(ThemeConstants.fontBodySm());
        subtitle.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subtitle);

        // Right Header Badges (Bell + Profile)
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        // Bell / Alerts Button
        bellBtn = new JButton("Alerts");
        bellBtn.setFont(ThemeConstants.fontLabelMd());
        bellBtn.putClientProperty("FlatLaf.styleClass", "outline");
        bellBtn.setPreferredSize(new Dimension(100, 38));
        bellBtn.setMargin(new Insets(0, 8, 0, 8));
        bellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        NotificationPopup popup = new NotificationPopup(notificationService, this::updateBellBadge);
        bellBtn.addActionListener(e -> popup.show(bellBtn, bellBtn.getWidth() - 340, bellBtn.getHeight()));
        rightHeader.add(bellBtn);

        // Separator line
        JSeparator headerSep = new JSeparator(SwingConstants.VERTICAL);
        headerSep.setPreferredSize(new Dimension(1, 38));
        headerSep.setForeground(ThemeConstants.BORDER);
        rightHeader.add(headerSep);

        // User Profile Badge
        JPanel profileBadge = new JPanel(new BorderLayout(8, 0));
        profileBadge.setOpaque(false);

        JPanel profileText = new JPanel();
        profileText.setOpaque(false);
        profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));

        JLabel uName = new JLabel("Dr. Sarah Chen");
        uName.setFont(ThemeConstants.fontLabelMd());
        uName.setForeground(ThemeConstants.ON_SURFACE);
        JLabel uRole = new JLabel("Chief Pharmacist");
        uRole.setFont(ThemeConstants.fontBodySm());
        uRole.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        profileText.add(uName);
        profileText.add(uRole);

        RoundedPanel avatar = new RoundedPanel(18);
        avatar.setBackground(Color.decode("#E0F2FE"));
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setLayout(new GridBagLayout());
        JLabel avLbl = new JLabel("SC");
        avLbl.setFont(ThemeConstants.fontLabelSm());
        avLbl.setForeground(ThemeConstants.PRIMARY);
        avatar.add(avLbl);

        profileBadge.add(profileText, BorderLayout.CENTER);
        profileBadge.add(avatar, BorderLayout.EAST);
        rightHeader.add(profileBadge);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(headerPanel);
        Component headerStrut = Box.createVerticalStrut(24);
        if (headerStrut instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        scrollContent.add(headerStrut);

        // Stats Card Panel (3 card layout)
        statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(statsPanel);
        Component statsStrut = Box.createVerticalStrut(24);
        if (statsStrut instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        scrollContent.add(statsStrut);

        // Grid 2-Column Layout (Predictor Card vs Combined Card Column)
        JPanel bottomRow = new JPanel(new GridBagLayout());
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- Left side: Stock Intelligence Predictor Card ---
        gbc.gridx = 0;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 12);
        
        RoundedPanel aiPredictorCard = new RoundedPanel(ThemeConstants.RADIUS_XL);
        aiPredictorCard.setBackground(Color.decode("#111C2D")); // Dark navy blue background matching HTML
        aiPredictorCard.setLayout(new BoxLayout(aiPredictorCard, BoxLayout.Y_AXIS));
        aiPredictorCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        
        // AI badge
        RoundedPanel aiBadge = new RoundedPanel(ThemeConstants.RADIUS_LG);
        aiBadge.setBackground(Color.decode("#1E293B"));
        aiBadge.setBorderColor(Color.decode("#475569"));
        aiBadge.setMaximumSize(new Dimension(140, 28));
        aiBadge.setLayout(new GridBagLayout());
        JLabel aiBadgeLbl = new JLabel("PHARMAAI ENGINE");
        aiBadgeLbl.setFont(ThemeConstants.fontLabelSm());
        aiBadgeLbl.setForeground(Color.decode("#38BDF8")); // bright sky blue
        aiBadge.add(aiBadgeLbl);
        aiBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel aiCardTitle = new JLabel("Stock Intelligence Predictor");
        aiCardTitle.setFont(ThemeConstants.fontHeadlineMd());
        aiCardTitle.setForeground(Color.WHITE);
        aiCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea aiDesc = new JTextArea("Our neural network predicts a 15% surge in Amoxicillin demand next month based on regional clinical data.");
        aiDesc.setFont(ThemeConstants.fontBodyMd());
        aiDesc.setForeground(Color.decode("#94A3B8"));
        aiDesc.setOpaque(false);
        aiDesc.setEditable(false);
        aiDesc.setLineWrap(true);
        aiDesc.setWrapStyleWord(true);
        aiDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel predictorStatsCol = new JPanel();
        predictorStatsCol.setOpaque(false);
        predictorStatsCol.setLayout(new BoxLayout(predictorStatsCol, BoxLayout.Y_AXIS));
        predictorStatsCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        predictorStatsCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel accPanel = new JPanel(new BorderLayout());
        accPanel.setOpaque(true);
        accPanel.setBackground(Color.decode("#1E293B"));
        accPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#334155"), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        JLabel accLbl = new JLabel("Accuracy Rate");
        accLbl.setFont(ThemeConstants.fontBodySm());
        accLbl.setForeground(Color.decode("#94A3B8"));
        JLabel accVal = new JLabel("98.2%");
        accVal.setFont(ThemeConstants.fontTitleMd());
        accVal.setForeground(Color.decode("#34D399")); // Green
        accPanel.add(accLbl, BorderLayout.WEST);
        accPanel.add(accVal, BorderLayout.EAST);

        JPanel sugPanel = new JPanel(new BorderLayout());
        sugPanel.setOpaque(true);
        sugPanel.setBackground(Color.decode("#1E293B"));
        sugPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#334155"), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        JLabel sugLbl = new JLabel("Optimization Suggestion");
        sugLbl.setFont(ThemeConstants.fontBodySm());
        sugLbl.setForeground(Color.decode("#94A3B8"));
        JLabel sugVal = new JLabel("+250 Units");
        sugVal.setFont(ThemeConstants.fontTitleMd());
        sugVal.setForeground(Color.decode("#38BDF8"));
        sugPanel.add(sugLbl, BorderLayout.WEST);
        sugPanel.add(sugVal, BorderLayout.EAST);

        predictorStatsCol.add(accPanel);
        predictorStatsCol.add(Box.createVerticalStrut(10));
        predictorStatsCol.add(sugPanel);

        JButton applyAiBtn = new JButton("Apply AI Recommendations");
        applyAiBtn.setFont(ThemeConstants.fontTitleMd());
        applyAiBtn.putClientProperty("FlatLaf.styleClass", "primary");
        applyAiBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        applyAiBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyAiBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "AI Recommendations applied: +250 Units Amoxicillin added to Order Prep."));

        aiPredictorCard.add(aiBadge);
        aiPredictorCard.add(Box.createVerticalStrut(20));
        aiPredictorCard.add(aiCardTitle);
        aiPredictorCard.add(Box.createVerticalStrut(12));
        aiPredictorCard.add(aiDesc);
        aiPredictorCard.add(Box.createVerticalStrut(24));
        aiPredictorCard.add(predictorStatsCol);
        aiPredictorCard.add(Box.createVerticalGlue());
        aiPredictorCard.add(applyAiBtn);

        bottomRow.add(aiPredictorCard, gbc);

        // --- Right side: Recent Activity & Inventory Health Card (Combined) ---
        gbc.gridx = 1;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 12, 0, 0);

        RoundedPanel rightColumnCard = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        rightColumnCard.setBackground(ThemeConstants.SURFACE);
        rightColumnCard.setHasShadow(true);
        rightColumnCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        rightColumnCard.setLayout(new BorderLayout(0, 16));

        // North: Recent Activity Header
        JPanel actHeader = new JPanel(new BorderLayout());
        actHeader.setOpaque(false);
        JLabel actTitle = new JLabel("Recent Activity");
        actTitle.setFont(ThemeConstants.fontTitleMd());
        actTitle.setForeground(ThemeConstants.ON_SURFACE);
        JButton viewAllBtn = new JButton("View All Logs");
        viewAllBtn.setFont(ThemeConstants.fontLabelSm());
        viewAllBtn.setForeground(ThemeConstants.PRIMARY);
        viewAllBtn.setBorder(null);
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Opening Audit Logs..."));
        actHeader.add(actTitle, BorderLayout.WEST);
        actHeader.add(viewAllBtn, BorderLayout.EAST);
        rightColumnCard.add(actHeader, BorderLayout.NORTH);

        // Center: Activity Table
        activityTableModel = new DefaultTableModel(new Object[]{"Activity", "Entity", "Status", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        activityTable = new JTable(activityTableModel);
        activityTable.setFont(ThemeConstants.fontBodyMd());
        activityTable.setRowHeight(52);
        activityTable.setShowGrid(false);
        activityTable.setIntercellSpacing(new Dimension(0, 0));
        activityTable.setBackground(ThemeConstants.SURFACE);
        activityTable.getTableHeader().setFont(ThemeConstants.fontLabelMd());
        activityTable.getTableHeader().setBackground(Color.decode("#F8FAFC"));
        activityTable.getTableHeader().setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        activityTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER));
        
        activityTable.getColumnModel().getColumn(0).setCellRenderer(new ActivityCellRenderer());
        activityTable.getColumnModel().getColumn(1).setCellRenderer(new BorderedCellRenderer(SwingConstants.LEFT, ThemeConstants.fontBodyMd(), ThemeConstants.ON_SURFACE));
        activityTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        activityTable.getColumnModel().getColumn(3).setCellRenderer(new BorderedCellRenderer(SwingConstants.RIGHT, ThemeConstants.fontBodySm(), ThemeConstants.ON_SURFACE_VARIANT));

        JScrollPane tableScroll = new JScrollPane(activityTable);
        tableScroll.setBorder(null);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setPreferredSize(new Dimension(0, 220));
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        
        JSeparator divider = new JSeparator(SwingConstants.HORIZONTAL);
        divider.setForeground(Color.decode("#E2E8F0"));
        centerPanel.add(divider, BorderLayout.SOUTH);
        
        rightColumnCard.add(centerPanel, BorderLayout.CENTER);

        // South: Inventory Health (7 Days) Chart Section
        JPanel chartSection = new JPanel(new BorderLayout(0, 12));
        chartSection.setOpaque(false);
        
        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        JLabel chartTitle = new JLabel("INVENTORY HEALTH (7 DAYS)");
        chartTitle.setFont(ThemeConstants.fontLabelMd());
        chartTitle.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        
        JPanel dotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        dotRow.setOpaque(false);
        Color[] dotColors = {Color.decode("#006591"), Color.decode("#006C49"), Color.decode("#EF4444")};
        for (Color c : dotColors) {
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(c);
                    g.fillOval(0, 0, 8, 8);
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(8, 8));
            dotRow.add(dot);
        }
        chartHeader.add(chartTitle, BorderLayout.WEST);
        chartHeader.add(dotRow, BorderLayout.EAST);
        
        InventoryHealthChart chart = new InventoryHealthChart();
        chart.setPreferredSize(new Dimension(0, 110));
        
        chartSection.add(chartHeader, BorderLayout.NORTH);
        chartSection.add(chart, BorderLayout.CENTER);
        
        rightColumnCard.add(chartSection, BorderLayout.SOUTH);

        bottomRow.add(rightColumnCard, gbc);
        scrollContent.add(bottomRow);

        // JScrollPane to force dashboard to be vertically scrollable and horizontally fixed/responsive
        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainContentPanel.add(mainScroll, BorderLayout.CENTER);

        layeredPane.add(mainContentPanel);
        
        // Floating Action Button (+)
        JButton fab = new JButton("+");
        fab.setFont(new Font("SansSerif", Font.PLAIN, 28));
        fab.putClientProperty("FlatLaf.styleClass", "primary");
        fab.setPreferredSize(new Dimension(56, 56));
        fab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();
                mainContentPanel.setBounds(0, 0, w, h);
                fab.setBounds(w - 88, h - 88, 56, 56);
            }
        });
        
        fab.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            if (owner instanceof MainFrame mf) {
                mf.showAddMedicineDialog();
            }
        });
        
        layeredPane.add(fab, JLayeredPane.PALETTE_LAYER);
    }

    private void updateBellBadge() {
        int count = notificationService.getUnreadCount();
        bellBtn.setText(count > 0 ? "Alerts (" + count + ")" : "Alerts");
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            refreshStats();
            refreshActivity();
            updateBellBadge();
        });
    }

    private void refreshStats() {
        statsPanel.removeAll();
        InventoryService.DashboardStats stats = controller.getDashboardStats();

        // 1. TOTAL SKU
        statsPanel.add(buildMockStatCard("TOTAL SKU", String.format("%,d", stats.totalSKU), 
                "+12 new items this week", Color.decode("#10B981"), Color.decode("#E0F2FE"), Color.decode("#0EA5E9")));
        
        // 2. LOW STOCK
        statsPanel.add(buildMockStatCard("LOW STOCK", String.valueOf(stats.lowStockCount), 
                stats.lowStockCount + " items (action needed)", Color.decode("#EF4444"), Color.decode("#FEF3C7"), Color.decode("#F59E0B")));

        // 3. EXPIRED / NEAR EXP.
        int expiredNear = stats.expiredCount + stats.lowStockCount / 4; // Mock near exp
        statsPanel.add(buildMockStatCard("EXPIRED / NEAR EXP.", String.valueOf(expiredNear), 
                "Next batch expires in 14 days", Color.decode("#64748B"), Color.decode("#FEE2E2"), Color.decode("#EF4444")));

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel buildMockStatCard(String title, String value, String subtext, Color subtextColor, Color iconBg, Color iconColor) {
        RoundedPanel card = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        card.setBackground(ThemeConstants.SURFACE);
        card.setHasShadow(true);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeConstants.fontLabelSm());
        titleLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(ThemeConstants.fontHeadlineLg());
        valLbl.setForeground(ThemeConstants.ON_SURFACE);

        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(ThemeConstants.fontBodySm());
        subLbl.setForeground(subtextColor);

        leftPanel.add(titleLbl);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(valLbl);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(subLbl);

        RoundedPanel iconBox = new RoundedPanel(12);
        iconBox.setBackground(iconBg);
        iconBox.setPreferredSize(new Dimension(40, 40));
        iconBox.setLayout(new GridBagLayout());
        
        JLabel iconLbl = new JLabel(title.startsWith("TOTAL") ? "BOX" : title.startsWith("LOW") ? "WARN" : "EXP");
        iconLbl.setFont(ThemeConstants.fontLabelSm());
        iconLbl.setForeground(iconColor);
        iconBox.add(iconLbl);

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(iconBox, BorderLayout.EAST);
        return card;
    }

    private void refreshActivity() {
        activityTableModel.setRowCount(0);
        List<InventoryTransaction> recent = controller.getRecentActivity(4);

        // Map database transactions and merge with system alert logs for high-fidelity rendering matching HTML
        // Row 1
        if (recent.size() > 0) {
            InventoryTransaction t1 = recent.get(0);
            activityTableModel.addRow(new Object[]{
                new ActivityData("Stock Intake", "Received Batch #" + (t1.getNotes() != null ? t1.getNotes() : "AX402"), "+", Color.decode("#006C49"), Color.decode("#D1FAE5")),
                t1.getMedicineName(),
                "SUCCESS",
                "2 mins ago"
            });
        } else {
            activityTableModel.addRow(new Object[]{
                new ActivityData("Stock Intake", "Received Batch #AX402", "+", Color.decode("#006C49"), Color.decode("#D1FAE5")),
                "Amoxicillin 500mg",
                "SUCCESS",
                "2 mins ago"
            });
        }

        // Row 2 (Mock alert)
        activityTableModel.addRow(new Object[]{
            new ActivityData("Expiration Alert", "Batch #KL982 expiring soon", "!", Color.decode("#EF4444"), Color.decode("#FEE2E2")),
            "Insulin Glargine",
            "ALERT",
            "1 hour ago"
        });

        // Row 3 (Mock system sync)
        activityTableModel.addRow(new Object[]{
            new ActivityData("System Backup", "Auto-sync with Central DB", "S", ThemeConstants.ON_SURFACE_VARIANT, Color.decode("#F1F5F9")),
            "Local Database",
            "SYSTEM",
            "4 hours ago"
        });

        // Row 4
        if (recent.size() > 1) {
            InventoryTransaction t2 = recent.get(1);
            activityTableModel.addRow(new Object[]{
                new ActivityData("Stock Dispensed", t2.getNotes() != null ? t2.getNotes() : "Order #9921 fulfillment", "-", Color.decode("#EF4444"), Color.decode("#FEE2E2")),
                t2.getMedicineName(),
                "SUCCESS",
                "5 hours ago"
            });
        } else {
            activityTableModel.addRow(new Object[]{
                new ActivityData("Stock Dispensed", "Order #9921 fulfillment", "-", Color.decode("#EF4444"), Color.decode("#FEE2E2")),
                "Paracetamol 250ml",
                "SUCCESS",
                "5 hours ago"
            });
        }
    }
}

// Data holder for double-line cell renderer
class ActivityData {
    String title;
    String desc;
    String iconText;
    Color iconColor;
    Color iconBgColor;
    
    public ActivityData(String title, String desc, String iconText, Color iconColor, Color iconBgColor) {
        this.title = title;
        this.desc = desc;
        this.iconText = iconText;
        this.iconColor = iconColor;
        this.iconBgColor = iconBgColor;
    }
}

// Custom cell renderer for double-line Activity cell
class ActivityCellRenderer extends JPanel implements TableCellRenderer {
    private final JLabel iconLbl = new JLabel();
    private final JLabel titleLbl = new JLabel();
    private final JLabel descLbl = new JLabel();
    private final RoundedPanel iconCircle;

    public ActivityCellRenderer() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F5F9")),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        iconCircle = new RoundedPanel(14);
        iconCircle.setPreferredSize(new Dimension(28, 28));
        iconCircle.setLayout(new GridBagLayout());
        
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        iconCircle.add(iconLbl);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLbl);
        textPanel.add(descLbl);

        add(iconCircle, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            titleLbl.setForeground(table.getSelectionForeground());
            descLbl.setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            titleLbl.setForeground(ThemeConstants.ON_SURFACE);
            descLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        }

        if (value instanceof ActivityData data) {
            titleLbl.setText(data.title);
            titleLbl.setFont(ThemeConstants.fontLabelMd());
            
            descLbl.setText(data.desc);
            descLbl.setFont(ThemeConstants.fontBodySm());
            
            iconLbl.setText(data.iconText);
            iconLbl.setForeground(data.iconColor);
            iconCircle.setBackground(data.iconBgColor);
        }
        return this;
    }
}

// Custom renderer for bordered cell text columns
class BorderedCellRenderer extends DefaultTableCellRenderer {
    public BorderedCellRenderer(int alignment, Font font, Color foreground) {
        setHorizontalAlignment(alignment);
        setFont(font);
        setForeground(foreground);
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F5F9")));
        return this;
    }
}

// Custom cell renderer for the transaction status badges (pills)
class StatusCellRenderer extends JPanel implements TableCellRenderer {
    private final StatusBadge successBadge = new StatusBadge("SUCCESS", Color.decode("#006C49"), Color.decode("#D1FAE5"));
    private final StatusBadge alertBadge = new StatusBadge("ALERT", Color.decode("#EF4444"), Color.decode("#FEE2E2"));
    private final StatusBadge systemBadge = new StatusBadge("SYSTEM", ThemeConstants.ON_SURFACE_VARIANT, Color.decode("#F1F5F9"));
    
    public StatusCellRenderer() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 12));
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F5F9")));
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        removeAll();
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        
        String status = String.valueOf(value);
        if ("SUCCESS".equalsIgnoreCase(status)) {
            add(successBadge);
        } else if ("ALERT".equalsIgnoreCase(status)) {
            add(alertBadge);
        } else {
            add(systemBadge);
        }
        
        return this;
    }
}

class InventoryHealthChart extends JPanel {
    public InventoryHealthChart() {
        setPreferredSize(new Dimension(0, 110));
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight() - 20;
        int barWidth = 32;
        int gap = (w - (barWidth * 7)) / 8;
        if (gap < 4) gap = 4;
        
        int[] heights = {75, 50, 100, 66, 80, 33, 75}; // Mon-Sun heights matching HTML chart (3/4, 1/2, full, 2/3, 4/5, 1/3, 3/4)
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        
        for (int i = 0; i < 7; i++) {
            int barHeight = (int) ((heights[i] / 100.0) * h);
            int x = gap + i * (barWidth + gap);
            int y = h - barHeight;
            
            // Draw background track round-rect
            g2.setColor(Color.decode("#F1F5F9")); 
            g2.fillRoundRect(x, 0, barWidth, h, 6, 6);
            
            // Draw active filled round-rect
            g2.setColor(Color.decode("#006591")); // Theme Blue matching primary/20
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);
            
            // Label rendering
            g2.setColor(ThemeConstants.ON_SURFACE_VARIANT);
            g2.setFont(ThemeConstants.fontBodySm());
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (barWidth - fm.stringWidth(days[i])) / 2;
            g2.drawString(days[i], tx, h + 15);
        }
        
        g2.dispose();
    }
}
