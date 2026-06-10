package com.pharmastock.view.components;

import com.pharmastock.model.Medicine;
import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

public class MedicineCard extends JPanel {

    private final Medicine medicine;
    private Runnable onClick;

    public MedicineCard(Medicine medicine) {
        this.medicine = medicine;
        
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.SURFACE);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(220, 150));

        // Left color bar indicator
        Color statusColor = getStatusColor(medicine.getStockStatus());
        JPanel leftIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(statusColor);
                g.fillRoundRect(0, 0, 4, getHeight(), 6, 6);
            }
        };
        leftIndicator.setOpaque(false);
        leftIndicator.setPreferredSize(new Dimension(4, 0));
        add(leftIndicator, BorderLayout.WEST);

        // Content Wrapper
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Top Row: Icon + Status Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Status Badge
        StatusBadge badge = new StatusBadge(medicine.getStockStatusLabel(),
                statusColor, getStatusBgColor(medicine.getStockStatus()));
        topRow.add(badge, BorderLayout.EAST);
        content.add(topRow);
        content.add(Box.createVerticalStrut(10));

        // Middle Row: Name + Category Subtitle
        JLabel nameLbl = new JLabel("<html><b>" + medicine.getMedicineName() + "</b></html>");
        nameLbl.setFont(ThemeConstants.fontTitleMd());
        nameLbl.setForeground(ThemeConstants.ON_SURFACE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catLbl = new JLabel(medicine.getCategory() + " • " + (medicine.getBatchNumber() != null ? "Generic" : "Standard"));
        catLbl.setFont(ThemeConstants.fontBodySm());
        catLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        catLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(nameLbl);
        content.add(Box.createVerticalStrut(2));
        content.add(catLbl);
        content.add(Box.createVerticalStrut(14));

        // Bottom Row: Current Stock vs Expiry Columns
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 8, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Column 1: Current Stock
        JPanel stockCol = new JPanel();
        stockCol.setOpaque(false);
        stockCol.setLayout(new BoxLayout(stockCol, BoxLayout.Y_AXIS));
        JLabel stockTitle = new JLabel("Current Stock");
        stockTitle.setFont(ThemeConstants.fontLabelSm());
        stockTitle.setForeground(ThemeConstants.OUTLINE);
        JLabel stockVal = new JLabel(String.format("%,d", medicine.getStockQuantity()) + " Units");
        stockVal.setFont(ThemeConstants.fontTitleMd());
        stockVal.setForeground(ThemeConstants.ON_SURFACE);
        stockCol.add(stockTitle);
        stockCol.add(stockVal);

        // Column 2: Expiry
        JPanel expCol = new JPanel();
        expCol.setOpaque(false);
        expCol.setLayout(new BoxLayout(expCol, BoxLayout.Y_AXIS));
        JLabel expTitle = new JLabel("Expiry");
        expTitle.setFont(ThemeConstants.fontLabelSm());
        expTitle.setForeground(ThemeConstants.OUTLINE);
        
        String dateStr = "N/A";
        if (medicine.getExpiryDate() != null) {
            dateStr = medicine.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yyyy"));
        }
        JLabel expVal = new JLabel(dateStr);
        expVal.setFont(ThemeConstants.fontTitleMd());
        expVal.setForeground(medicine.getStockStatus() == Medicine.StockStatus.EXPIRED ? ThemeConstants.DANGER : ThemeConstants.ON_SURFACE);
        
        expCol.add(expTitle);
        expCol.add(expVal);

        bottomRow.add(stockCol);
        bottomRow.add(expCol);
        content.add(bottomRow);

        add(content, BorderLayout.CENTER);

        // Hover events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Color.decode("#F1F5F9"));
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(ThemeConstants.SURFACE);
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "💊";
        return switch (category.toLowerCase()) {
            case "antibiotik" -> "💊";
            case "analgesik" -> "🪱";
            case "sirup" -> "🧪";
            case "vitamin", "suplemen" -> "🍏";
            default -> "💊";
        };
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    private Color getStatusColor(Medicine.StockStatus status) {
        return switch (status) {
            case IN_STOCK -> ThemeConstants.SECONDARY;
            case LOW_STOCK, NEAR_EXPIRY -> ThemeConstants.WARNING;
            case OUT_OF_STOCK, EXPIRED -> ThemeConstants.DANGER;
        };
    }

    private Color getStatusBgColor(Medicine.StockStatus status) {
        return switch (status) {
            case IN_STOCK -> ThemeConstants.SECONDARY_TINT_10;
            case LOW_STOCK, NEAR_EXPIRY -> ThemeConstants.WARNING_TINT_10;
            case OUT_OF_STOCK, EXPIRED -> ThemeConstants.DANGER_TINT_10;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw card background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConstants.RADIUS_XL, ThemeConstants.RADIUS_XL);
        
        // Draw card border
        g2.setColor(ThemeConstants.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ThemeConstants.RADIUS_XL, ThemeConstants.RADIUS_XL);
        
        g2.dispose();
    }
}
