package com.pharmastock.view.inventory;

import com.pharmastock.controller.InventoryController;
import com.pharmastock.model.Medicine;
import com.pharmastock.service.NotificationService;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.MainFrame;
import com.pharmastock.view.components.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends BasePanel {

    private final InventoryController controller;
    private final NotificationService notificationService;

    private JPanel listPanel;
    private JLabel pageLabel;
    private JTextField searchField;
    private JLabel showingCountLabel;
    private JTable batchTable;
    private DefaultTableModel batchTableModel;
    private JButton bellBtn;
    private final List<CategoryChip> chips = new ArrayList<>();

    public InventoryPanel(InventoryController controller, NotificationService notificationService) {
        this.controller = controller;
        this.notificationService = notificationService;
        
        setBackground(ThemeConstants.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        
        initUI();
        refreshData();
    }

    @Override
    protected void initUI() {
        ScrollablePanel scrollContent = new ScrollablePanel();
        scrollContent.setOpaque(false);
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));

        // 1. Top Navigation Bar (Search bar, Tambah Stok, Lonceng)
        JPanel topNav = new JPanel(new BorderLayout(14, 0));
        topNav.setOpaque(false);
        topNav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        searchField = new JTextField();
        searchField.setFont(ThemeConstants.fontBodyMd());
        searchField.putClientProperty("JTextField.placeholderText", "Search medication by name, SKU, or category...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                controller.searchMedicines(searchField.getText().trim());
                refreshData();
            }
        });
        topNav.add(searchField, BorderLayout.CENTER);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton addBtn = new JButton("+ Tambah Stok");
        addBtn.setFont(ThemeConstants.fontTitleMd());
        addBtn.putClientProperty("FlatLaf.styleClass", "primary");
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(140, 38));
        addBtn.addActionListener(e -> showAddDialog());
        rightActions.add(addBtn);

        bellBtn = new JButton("Alerts");
        bellBtn.setFont(ThemeConstants.fontLabelMd());
        bellBtn.putClientProperty("FlatLaf.styleClass", "outline");
        bellBtn.setPreferredSize(new Dimension(100, 38));
        bellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        NotificationPopup popup = new NotificationPopup(notificationService, this::updateBellBadge);
        bellBtn.addActionListener(e -> popup.show(bellBtn, bellBtn.getWidth() - 340, bellBtn.getHeight()));
        rightActions.add(bellBtn);

        topNav.add(rightActions, BorderLayout.EAST);
        topNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(topNav);
        scrollContent.add(Box.createVerticalStrut(20));

        // 2. Filter chips row & count label
        JPanel chipsRow = new JPanel(new BorderLayout());
        chipsRow.setOpaque(false);
        chipsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipPanel.setOpaque(false);

        CategoryChip allChip = new CategoryChip("Semua");
        allChip.setSelected(true);
        allChip.addActionListener(e -> {
            clearChipSelection();
            allChip.setSelected(true);
            controller.filterByCategory("");
            controller.filterByStatus("");
            refreshData();
        });
        chips.add(allChip);
        chipPanel.add(allChip);

        // Standard filter categories
        String[] standardCats = {"Antibiotik", "Analgesik", "Sirup", "Suplemen"};
        for (String cat : standardCats) {
            CategoryChip chip = new CategoryChip(cat);
            chip.addActionListener(e -> {
                clearChipSelection();
                chip.setSelected(true);
                controller.filterByCategory(cat);
                refreshData();
            });
            chips.add(chip);
            chipPanel.add(chip);
        }
        chipsRow.add(chipPanel, BorderLayout.WEST);

        // Showing Count Label on the right
        showingCountLabel = new JLabel("Showing 0 Medicines");
        showingCountLabel.setFont(ThemeConstants.fontBodySm());
        showingCountLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        showingCountLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 10));
        chipsRow.add(showingCountLabel, BorderLayout.EAST);
        chipsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        scrollContent.add(chipsRow);
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. 4-Column Grid Panel for Cards
        listPanel = new JPanel(new GridLayout(0, 4, 16, 16));
        listPanel.setOpaque(false);
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(listPanel);
        scrollContent.add(Box.createVerticalStrut(28));

        // 4. Batch Details Section
        JPanel batchSection = new JPanel(new BorderLayout());
        batchSection.setOpaque(false);
        batchSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel batchHeader = new JPanel(new BorderLayout());
        batchHeader.setOpaque(false);
        batchHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel batchTitle = new JLabel("Batch Details");
        batchTitle.setFont(ThemeConstants.fontHeadlineMd());
        batchTitle.setForeground(ThemeConstants.ON_SURFACE);

        JButton exportBtn = new JButton("Export Report");
        exportBtn.setFont(ThemeConstants.fontLabelMd());
        exportBtn.putClientProperty("FlatLaf.styleClass", "outline");
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mengekspor Laporan Inventaris..."));
        
        batchHeader.add(batchTitle, BorderLayout.WEST);
        batchHeader.add(exportBtn, BorderLayout.EAST);
        batchSection.add(batchHeader, BorderLayout.NORTH);

        // Batch Table
        String[] cols = {"MEDICINE CODE", "BATCH NUMBER", "SUPPLIER", "ENTRY DATE", "EXPIRY DATE", "STOCK", "ACTION"};
        batchTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        batchTable = new JTable(batchTableModel);
        batchTable.setFont(ThemeConstants.fontBodyMd());
        batchTable.setRowHeight(38);
        batchTable.getTableHeader().setFont(ThemeConstants.fontLabelMd());
        batchTable.getTableHeader().setBackground(ThemeConstants.SURFACE_CONTAINER_LOW);
        batchTable.setSelectionBackground(ThemeConstants.PRIMARY_TINT_10);
        
        // Apply custom cell renderer for Stock Progress Bar column
        batchTable.getColumnModel().getColumn(5).setCellRenderer(new ProgressCellRenderer());
        
        JScrollPane tableScroll = new JScrollPane(batchTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER, 1));
        tableScroll.getViewport().setBackground(ThemeConstants.SURFACE);
        tableScroll.setPreferredSize(new Dimension(0, 180));
        
        batchSection.add(tableScroll, BorderLayout.CENTER);
        scrollContent.add(batchSection);

        // Main Layout wrapper
        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(mainScroll, BorderLayout.CENTER);

        // 5. Pagination Bar & Scanner Circular Float Button
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Left Pagination Label
        pageLabel = new JLabel("Halaman 1 / 1");
        pageLabel.setFont(ThemeConstants.fontBodyMd());
        pageLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        bottomBar.add(pageLabel, BorderLayout.WEST);

        // Right Pagination actions + Float Scanner Btn
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        JButton prevBtn = new JButton("Sebelumnya");
        prevBtn.setFont(ThemeConstants.fontBodyMd());
        prevBtn.putClientProperty("FlatLaf.styleClass", "outline");
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.addActionListener(e -> {
            controller.previousPage();
            refreshData();
        });

        JButton nextBtn = new JButton("Selanjutnya");
        nextBtn.setFont(ThemeConstants.fontBodyMd());
        nextBtn.putClientProperty("FlatLaf.styleClass", "outline");
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> {
            controller.nextPage();
            refreshData();
        });

        JButton scanBtn = new JButton("SCAN");
        scanBtn.setFont(ThemeConstants.fontLabelSm());
        scanBtn.putClientProperty("FlatLaf.styleClass", "primary");
        scanBtn.setPreferredSize(new Dimension(60, 44));
        scanBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        scanBtn.setToolTipText("Scan Barcode");
        scanBtn.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(this, "Masukkan kode barcode:", "Scan Barcode", JOptionPane.PLAIN_MESSAGE);
            if (code != null && !code.isBlank()) {
                var med = controller.lookupByBarcode(code.trim());
                if (med.isPresent()) {
                    showDetailDialog(med.get());
                } else {
                    JOptionPane.showMessageDialog(this, "Obat dengan barcode '" + code.trim() + "' tidak ditemukan.", "Pencarian", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        rightPanel.add(prevBtn);
        rightPanel.add(nextBtn);
        rightPanel.add(scanBtn);
        bottomBar.add(rightPanel, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void updateBellBadge() {
        int count = notificationService.getUnreadCount();
        bellBtn.setText(count > 0 ? "Alerts (" + count + ")" : "Alerts");
    }

    @Override
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            listPanel.removeAll();
            batchTableModel.setRowCount(0);
            updateBellBadge();

            List<Medicine> medicines = controller.loadInventory();
            showingCountLabel.setText("Showing " + medicines.size() + " Medicines");

            // Build grid cards
            for (Medicine med : medicines) {
                MedicineCard card = new MedicineCard(med);
                card.setOnClick(() -> showDetailDialog(med));
                listPanel.add(card);

                // Add Row to Batch Details table
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
                String entryDate = med.getCreatedAt() != null ? med.getCreatedAt().format(fmt) : "12 Oct 2023";
                String expDate = med.getExpiryDate() != null ? med.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "-";
                
                int maxStock = Math.max(med.getMinimumStock() * 4, med.getStockQuantity());
                int stockPercent = (int) Math.min(100, ((double) med.getStockQuantity() / Math.max(1, maxStock)) * 100);

                batchTableModel.addRow(new Object[]{
                        med.getMedicineCode(),
                        med.getBatchNumber() != null ? med.getBatchNumber() : "BCH-99281",
                        med.getSupplierName() != null ? med.getSupplierName() : "PT BioFarma Corp.",
                        entryDate,
                        expDate,
                        stockPercent, // rendered by JProgressBar renderer
                        "..."
                });
            }

            // Append Add New Medicine card at the end
            AddNewMedicineCard addCard = new AddNewMedicineCard(this::showAddDialog);
            listPanel.add(addCard);

            int total = controller.getTotalPages();
            int current = controller.getCurrentPage() + 1;
            pageLabel.setText("Halaman " + current + " / " + total);

            listPanel.revalidate();
            listPanel.repaint();
        });
    }

    private void clearChipSelection() {
        for (CategoryChip chip : chips) {
            chip.setSelected(false);
        }
    }

    private void showAddDialog() {
        AddMedicineDialog dialog = new AddMedicineDialog(
                SwingUtilities.getWindowAncestor(this), controller);
        dialog.setVisible(true);
        refreshData();
    }

    private void showDetailDialog(Medicine medicine) {
        MedicineDetailDialog dialog = new MedicineDetailDialog(
                SwingUtilities.getWindowAncestor(this), medicine, controller);
        dialog.setVisible(true);
        refreshData();
    }
}

class AddNewMedicineCard extends JPanel {
    public AddNewMedicineCard(Runnable onClick) {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, 140));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onClick.run();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw dashed border
        float[] dash = {6f, 4f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2.setColor(ThemeConstants.OUTLINE_VARIANT);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ThemeConstants.RADIUS_XL, ThemeConstants.RADIUS_XL);
        
        // Plus symbol
        g2.setFont(new Font("SansSerif", Font.PLAIN, 28));
        g2.setColor(ThemeConstants.OUTLINE);
        FontMetrics fm = g2.getFontMetrics();
        String plus = "+";
        int px = (getWidth() - fm.stringWidth(plus)) / 2;
        g2.drawString(plus, px, getHeight() / 2 - 10);
        
        // Text caption
        g2.setFont(ThemeConstants.fontBodySm());
        fm = g2.getFontMetrics();
        String label = "Add New Medicine";
        int lx = (getWidth() - fm.stringWidth(label)) / 2;
        g2.drawString(label, lx, getHeight() / 2 + 15);
        
        g2.dispose();
    }
}

class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {
    public ProgressCellRenderer() {
        super(0, 100);
        setStringPainted(false);
        setBorderPainted(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        int val = 0;
        if (value instanceof Number num) {
            val = num.intValue();
        }
        setValue(val);
        
        // Color bounds
        if (val > 50) {
            setForeground(Color.decode("#10B981")); // Green
        } else if (val > 15) {
            setForeground(Color.decode("#F59E0B")); // Yellow
        } else {
            setForeground(Color.decode("#EF4444")); // Red
        }
        
        setBackground(Color.decode("#F1F5F9")); // Track color
        return this;
    }
}
