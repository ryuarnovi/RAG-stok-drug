package com.pharmastock.view.report;

import com.pharmastock.service.ReportService;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.components.BasePanel;
import com.pharmastock.view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;

public class ReportPanel extends BasePanel {

    private final ReportService reportService;

    public ReportPanel(ReportService reportService) {
        this.reportService = reportService;
        setBackground(ThemeConstants.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Laporan");
        title.setFont(ThemeConstants.fontHeadlineLg());
        title.setForeground(ThemeConstants.ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(24));

        // Report cards grid
        JPanel grid = new JPanel(new GridLayout(3, 2, 16, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(buildReportCard("Laporan Inventaris",
                "Seluruh data obat dengan stok dan status", "inventory"));
        grid.add(buildReportCard("Laporan Obat Kadaluarsa",
                "Obat yang telah/akan kadaluarsa", "expired"));
        grid.add(buildReportCard("Laporan Supplier",
                "Daftar supplier dan jumlah obat", "supplier"));

        content.add(grid);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildReportCard(String name, String description, String type) {
        RoundedPanel card = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        card.setBackground(ThemeConstants.SURFACE);
        card.setHasShadow(true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(ThemeConstants.fontTitleMd());
        nameLabel.setForeground(ThemeConstants.ON_SURFACE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(ThemeConstants.fontBodySm());
        descLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton pdfBtn = new JButton("PDF");
        pdfBtn.setFont(ThemeConstants.fontLabelMd());
        pdfBtn.putClientProperty("FlatLaf.styleClass", "primary");
        pdfBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pdfBtn.addActionListener(e -> generateReport(type, "PDF"));

        JButton excelBtn = new JButton("Excel");
        excelBtn.setFont(ThemeConstants.fontLabelMd());
        excelBtn.putClientProperty("FlatLaf.styleClass", "secondary");
        excelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        excelBtn.addActionListener(e -> generateReport(type, "EXCEL"));

        btnRow.add(pdfBtn);
        btnRow.add(excelBtn);

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(btnRow);

        return card;
    }

    private void generateReport(String type, String format) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String filePath = switch (type) {
                case "inventory" -> reportService.generateInventoryReport(format);
                case "expired" -> reportService.generateExpiredReport(format);
                case "movement" -> reportService.generateStockMovementReport(
                        LocalDate.now().minusMonths(1), LocalDate.now(), format);
                case "supplier" -> reportService.generateSupplierReport(format);
                default -> throw new IllegalArgumentException("Tipe laporan tidak valid.");
            };
            JOptionPane.showMessageDialog(this,
                    "Laporan berhasil dibuat:\n" + filePath,
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            // Open file
            Desktop.getDesktop().open(new File(filePath));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal membuat laporan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void refreshData() {
        // No data to reload on report panel
    }
}
