package com.pharmastock.view.ai;

import com.pharmastock.controller.AIController;
import com.pharmastock.service.NotificationService;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.MainFrame;
import com.pharmastock.view.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AIChatPanel extends BasePanel {

    private final AIController controller;
    private NotificationService notificationService;
    
    private ScrollablePanel chatArea;
    private JTextField inputField;
    private JScrollPane scrollPane;
    private JButton bellBtn;

    public AIChatPanel(AIController controller) {
        this.controller = controller;
        
        setBackground(ThemeConstants.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        
        initUI();
        addWelcomeMessage();
        resolveNotificationService();
    }

    @Override
    protected void initUI() {
        // Split Layout: Left Suggested Queries, Right Chat Area
        JPanel mainSplit = new JPanel(new GridBagLayout());
        mainSplit.setOpaque(false);
        add(mainSplit, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- LEFT COLUMN: Suggested Queries ---
        gbc.gridx = 0;
        gbc.weightx = 0.30;
        gbc.insets = new Insets(0, 0, 0, 16);

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel sugTitle = new JLabel("Suggested Queries");
        sugTitle.setFont(ThemeConstants.fontTitleMd());
        sugTitle.setForeground(ThemeConstants.ON_SURFACE);
        sugTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(sugTitle);
        leftPanel.add(Box.createVerticalStrut(14));

        // Add 4 query cards
        leftPanel.add(buildSuggestedQueryCard("Medicine Expiry List", "Check items expiring in 30 days"));
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(buildSuggestedQueryCard("Low Stock Alert", "Analyze reorder thresholds"));
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(buildSuggestedQueryCard("Daily Sales Summary", "View today's clinical turnover"));
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(buildSuggestedQueryCard("Audit Readiness", "Generate narcotics compliance report"));
        leftPanel.add(Box.createVerticalGlue());

        // Operational Status Tracker at bottom-left
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statusPanel.setBackground(Color.decode("#F0F9FF"));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E0F2FE"), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        statusPanel.setMaximumSize(new Dimension(200, 32));
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statusDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.decode("#10B981")); // Operational green
                g.fillOval(0, 0, 8, 8);
            }
        };
        statusDot.setOpaque(false);
        statusDot.setPreferredSize(new Dimension(8, 8));

        JLabel statusLbl = new JLabel("AI Status: OPERATIONAL");
        statusLbl.setFont(ThemeConstants.fontLabelSm());
        statusLbl.setForeground(ThemeConstants.PRIMARY);

        statusPanel.add(statusDot);
        statusPanel.add(statusLbl);
        leftPanel.add(statusPanel);

        mainSplit.add(leftPanel, gbc);

        // --- RIGHT COLUMN: Chat Thread ---
        gbc.gridx = 1;
        gbc.weightx = 0.70;
        gbc.insets = new Insets(0, 16, 0, 0);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // Chat Header (links + profile/bell actions)
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setOpaque(false);
        chatHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel headerLinks = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        headerLinks.setOpaque(false);
        
        JLabel brandLogo = new JLabel("PharmaStock");
        brandLogo.setFont(ThemeConstants.fontTitleMd());
        brandLogo.setForeground(ThemeConstants.PRIMARY);
        headerLinks.add(brandLogo);

        chatHeader.add(headerLinks, BorderLayout.WEST);

        // Bell and profile icons
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        bellBtn = new JButton("Alerts");
        bellBtn.setFont(ThemeConstants.fontLabelMd());
        bellBtn.putClientProperty("FlatLaf.styleClass", "outline");
        bellBtn.setPreferredSize(new Dimension(100, 36));
        bellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightHeader.add(bellBtn);

        RoundedPanel profileIcon = new RoundedPanel(18);
        profileIcon.setBackground(Color.decode("#F1F5F9"));
        profileIcon.setPreferredSize(new Dimension(36, 36));
        profileIcon.setLayout(new GridBagLayout());
        JLabel pLbl = new JLabel("USER");
        pLbl.setFont(ThemeConstants.fontLabelSm());
        profileIcon.add(pLbl);
        rightHeader.add(profileIcon);
        chatHeader.add(rightHeader, BorderLayout.EAST);

        rightPanel.add(chatHeader, BorderLayout.NORTH);

        // Chat Bubble Scroll Area
        chatArea = new ScrollablePanel();
        chatArea.setOpaque(false);
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom input actions & disclaimer
        JPanel chatBottom = new JPanel();
        chatBottom.setOpaque(false);
        chatBottom.setLayout(new BoxLayout(chatBottom, BoxLayout.Y_AXIS));

        // Suggested Pills Row
        JPanel pillsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pillsRow.setOpaque(false);
        String[] pills = {"Compare Vendors", "Usage Forecast", "Audit Logs"};
        for (String p : pills) {
            JButton pillBtn = new JButton(p);
            pillBtn.setFont(ThemeConstants.fontLabelSm());
            pillBtn.putClientProperty("FlatLaf.styleClass", "outline");
            pillBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pillBtn.addActionListener(e -> {
                inputField.setText("Show " + p.toLowerCase());
                sendMessage();
            });
            pillsRow.add(pillBtn);
        }
        pillsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatBottom.add(pillsRow);
        chatBottom.add(Box.createVerticalStrut(8));

        // Input bar
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setOpaque(false);
        inputBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton attachBtn = new JButton("Attach");
        attachBtn.setFont(ThemeConstants.fontLabelSm());
        attachBtn.putClientProperty("FlatLaf.styleClass", "outline");
        attachBtn.setPreferredSize(new Dimension(65, 38));
        attachBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Lampiran belum didukung pada demo ini."));
        inputBar.add(attachBtn, BorderLayout.WEST);

        inputField = new JTextField();
        inputField.setFont(ThemeConstants.fontBodyMd());
        inputField.putClientProperty("JTextField.placeholderText", "Ask PharmaStock Assistant anything...");
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        inputBar.add(inputField, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        sendBtn.setFont(ThemeConstants.fontLabelSm());
        sendBtn.putClientProperty("FlatLaf.styleClass", "primary");
        sendBtn.setPreferredSize(new Dimension(60, 38));
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendMessage());
        inputBar.add(sendBtn, BorderLayout.EAST);
        chatBottom.add(inputBar);
        chatBottom.add(Box.createVerticalStrut(10));

        // Disclaimer caption
        JLabel disclaimer = new JLabel("PharmaStock AI can provide clinical data assistance. Always verify medication orders manually.");
        disclaimer.setFont(ThemeConstants.fontBodySm());
        disclaimer.setForeground(ThemeConstants.OUTLINE);
        disclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel disWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        disWrapper.setOpaque(false);
        disWrapper.add(disclaimer);
        disWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatBottom.add(disWrapper);

        rightPanel.add(chatBottom, BorderLayout.SOUTH);
        mainSplit.add(rightPanel, gbc);
    }

    private void updateBellBadge() {
        resolveNotificationService();
        if (notificationService == null) return;
        int count = notificationService.getUnreadCount();
        bellBtn.setText(count > 0 ? "Alerts (" + count + ")" : "Alerts");
    }

    private void resolveNotificationService() {
        if (notificationService != null) return;
        SwingUtilities.invokeLater(() -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            if (parent instanceof MainFrame mf) {
                this.notificationService = mf.getNotificationService();
                if (notificationService != null) {
                    NotificationPopup popup = new NotificationPopup(notificationService, this::updateBellBadge);
                    bellBtn.addActionListener(e -> popup.show(bellBtn, bellBtn.getWidth() - 340, bellBtn.getHeight()));
                    int count = notificationService.getUnreadCount();
                    bellBtn.setText(count > 0 ? "Alerts (" + count + ")" : "Alerts");
                }
            }
        });
    }

    private JPanel buildSuggestedQueryCard(String title, String desc) {
        RoundedPanel card = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        card.setBackground(ThemeConstants.SURFACE);
        card.setHasShadow(true);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeConstants.fontLabelMd());
        titleLbl.setForeground(ThemeConstants.ON_SURFACE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(ThemeConstants.fontBodySm());
        descLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(descLbl);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                inputField.setText("Show " + title.toLowerCase());
                sendMessage();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(Color.decode("#F1F5F9"));
                card.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(ThemeConstants.SURFACE);
                card.repaint();
            }
        });
        return card;
    }

    private void addWelcomeMessage() {
        addBotMessage("Halo! Saya asisten PharmaStock. Ada yang bisa saya bantu dengan inventaris Anda hari ini?");
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        addUserMessage(message);
        inputField.setText("");

        // Process in background
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return controller.sendMessage(message);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    addBotMessage(response);
                } catch (Exception ex) {
                    addBotMessage("Maaf, terjadi kesalahan: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void addUserMessage(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        ChatBubble bubble = new ChatBubble(message, time, true);
        chatArea.add(bubble);
        chatArea.add(Box.createVerticalStrut(8));
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        ChatBubble bubble = new ChatBubble(message, time, false);

        // Check if query is looking for low stock reordering
        if (message.toLowerCase().contains("reorder") || message.toLowerCase().contains("low stock") || message.toLowerCase().contains("stok")) {
            JPanel cardContainer = buildSmartReorderCard();
            bubble.add(Box.createVerticalStrut(10));
            bubble.add(cardContainer);
        }

        chatArea.add(bubble);
        chatArea.add(Box.createVerticalStrut(8));
        scrollToBottom();
    }

    private JPanel buildSmartReorderCard() {
        RoundedPanel card = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        card.setBackground(ThemeConstants.SURFACE);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(380, 180));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Smart Reorder Plan");
        title.setFont(ThemeConstants.fontTitleMd());
        title.setForeground(ThemeConstants.PRIMARY);
        header.add(title);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(header);
        card.add(Box.createVerticalStrut(8));

        // Table
        String[] cols = {"Medication", "Current", "Suggested", "Cost Est."};
        Object[][] data = {
                {"Metformin 500mg", "12", "+200", "$45.00"},
                {"Atorvastatin 20mg", "5", "+150", "$82.50"}
        };
        JTable table = new JTable(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(ThemeConstants.fontBodySm());
        table.setRowHeight(24);
        table.getTableHeader().setFont(ThemeConstants.fontLabelSm());
        table.getTableHeader().setBackground(ThemeConstants.SURFACE_CONTAINER_LOW);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER, 1));
        scroll.setPreferredSize(new Dimension(350, 80));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(scroll);
        card.add(Box.createVerticalStrut(10));

        // Send Button
        JButton sendProcBtn = new JButton("Send Reorder to Procurement");
        sendProcBtn.setFont(ThemeConstants.fontLabelMd());
        sendProcBtn.putClientProperty("FlatLaf.styleClass", "primary");
        sendProcBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendProcBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        sendProcBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendProcBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Order sent successfully to PT Kimia Farma & PT Kalbe Farma."));
        card.add(sendProcBtn);

        return card;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            chatArea.revalidate();
            chatArea.repaint();
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(this::updateBellBadge);
    }
}
