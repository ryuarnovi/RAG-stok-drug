package com.pharmastock.view.inventory;

import com.pharmastock.controller.InventoryController;
import com.pharmastock.model.Medicine;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.components.BaseDialog;
import com.pharmastock.view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AddMedicineDialog extends BaseDialog {

    private final InventoryController controller;
    private final Medicine editMedicine; // null untuk mode tambah

    private JTextField nameField, codeField, batchField, unitField;
    private JTextField purchasePriceField, sellingPriceField, minStockField;
    private JTextField qtyField;
    private JComboBox<String> categoryCombo;
    private JTextField expiryField;
    private JTextArea notesArea;

    private JPanel previewPanel;
    private ScannerVisualPanel scannerVisual;

    public AddMedicineDialog(Window owner, InventoryController controller) {
        this(owner, controller, null);
    }

    public AddMedicineDialog(Window owner, InventoryController controller, Medicine editMedicine) {
        super(owner, editMedicine != null ? "Edit Obat" : "Tambah Stok Baru", true);
        this.controller = controller;
        this.editMedicine = editMedicine;
        setSize(850, 680);
        setLocationRelativeTo(owner);
        setResizable(true); // Allow resizing for responsiveness
        getContentPane().setBackground(ThemeConstants.SURFACE);
        initUI();
        if (editMedicine != null) populateFields();
    }

    @Override
    protected void initUI() {
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setBackground(ThemeConstants.SURFACE);
        container.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // 1. HEADER ROW (Breadcrumb + Title)
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel breadcrumb = new JLabel("Inventory / " + (editMedicine != null ? "Edit Obat" : "Tambah Stok"));
        breadcrumb.setFont(ThemeConstants.fontBodySm());
        breadcrumb.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        backBtn.setBorder(null);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        titleRow.add(backBtn);

        JLabel titleLbl = new JLabel(editMedicine != null ? "Edit Stok Obat" : "Manajemen Stok Baru");
        titleLbl.setFont(ThemeConstants.fontHeadlineMd());
        titleLbl.setForeground(ThemeConstants.ON_SURFACE);
        titleRow.add(titleLbl);

        headerPanel.add(breadcrumb);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(titleRow);
        container.add(headerPanel, BorderLayout.NORTH);

        // 2. MIDDLE SPLIT PANEL (Left: Scanner/Barcode, Right: Form)
        JPanel splitPanel = new JPanel(new GridBagLayout());
        splitPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- LEFT COLUMN (Scanner or Generated Barcode Preview) ---
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 16);

        RoundedPanel leftCard = new RoundedPanel(ThemeConstants.RADIUS_XL);
        leftCard.setBackground(Color.decode("#EFF6FF")); // Light blue tint
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        // Preview container holds either the custom visual scanner drawing or the generated barcode image label
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setOpaque(false);
        previewPanel.setPreferredSize(new Dimension(200, 150));
        previewPanel.setMaximumSize(new Dimension(200, 150));
        previewPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scannerVisual = new ScannerVisualPanel();
        previewPanel.add(scannerVisual, BorderLayout.CENTER);

        JLabel inputCepatTitle = new JLabel("Barcode");
        inputCepatTitle.setFont(ThemeConstants.fontTitleMd());
        inputCepatTitle.setForeground(Color.decode("#1E40AF")); // Dark blue
        inputCepatTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea inputCepatDesc = new JTextArea("Masukkan kode obat di samping atau gunakan scanner untuk memicu barcode secara realtime.");
        inputCepatDesc.setFont(ThemeConstants.fontBodySm());
        inputCepatDesc.setForeground(Color.decode("#1E3A8A")); // Slate blue
        inputCepatDesc.setOpaque(false);
        inputCepatDesc.setEditable(false);
        inputCepatDesc.setLineWrap(true);
        inputCepatDesc.setWrapStyleWord(true);
        inputCepatDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputCepatDesc.setMaximumSize(new Dimension(180, 50));

        JButton startScanBtn = new JButton("Mulai Scan");
        startScanBtn.setFont(ThemeConstants.fontLabelMd());
        startScanBtn.putClientProperty("FlatLaf.styleClass", "outline");
        startScanBtn.setForeground(Color.decode("#2563EB"));
        startScanBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startScanBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startScanBtn.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(this, "Masukkan kode barcode:", "Scan Barcode", JOptionPane.PLAIN_MESSAGE);
            if (code != null && !code.isBlank()) {
                codeField.setText(code.trim());
                var med = controller.lookupByBarcode(code.trim());
                if (med.isPresent()) {
                    fillFromMedicine(med.get());
                } else {
                    JOptionPane.showMessageDialog(this, "Obat dengan barcode '" + code.trim() + "' tidak ditemukan.", "Pencarian", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        leftCard.add(Box.createVerticalGlue());
        leftCard.add(previewPanel);
        leftCard.add(Box.createVerticalStrut(16));
        leftCard.add(inputCepatTitle);
        leftCard.add(Box.createVerticalStrut(8));
        leftCard.add(inputCepatDesc);
        leftCard.add(Box.createVerticalStrut(24));
        leftCard.add(startScanBtn);
        leftCard.add(Box.createVerticalGlue());
        splitPanel.add(leftCard, gbc);

        // --- RIGHT COLUMN (Form fields) ---
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // Form Fields
        nameField = new JTextField();
        nameField.setFont(ThemeConstants.fontBodyMd());
        nameField.putClientProperty("JTextField.placeholderText", "Contoh: Paracetamol 500mg");
        JPanel nameP = createFieldPanel("Nama Obat", nameField);

        codeField = new JTextField();
        codeField.setFont(ThemeConstants.fontBodyMd());
        codeField.putClientProperty("JTextField.placeholderText", "MED-XXX");
        
        // Real-time Barcode generation listener
        codeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handleCodeUpdate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { handleCodeUpdate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { handleCodeUpdate(); }
            private void handleCodeUpdate() {
                SwingUtilities.invokeLater(() -> updateBarcodePreview(codeField.getText().trim()));
            }
        });
        JPanel codeP = createFieldPanel("Kode Obat", codeField);

        categoryCombo = new JComboBox<>(new String[]{
                "Antibiotik", "Analgesik", "Antihistamin", "Anti-inflamasi",
                "Antidiabetes", "Antihipertensi", "Vitamin", "Gastrointestinal",
                "Kortikosteroid", "Respiratori", "Sedatif"
        });
        categoryCombo.setFont(ThemeConstants.fontBodyMd());
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        JPanel catP = createFieldPanel("Kategori", categoryCombo);

        expiryField = new JTextField();
        expiryField.setFont(ThemeConstants.fontBodyMd());
        expiryField.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        JPanel expP = createFieldPanel("Tanggal Kedaluwarsa", expiryField);

        batchField = new JTextField();
        batchField.setFont(ThemeConstants.fontBodyMd());
        batchField.putClientProperty("JTextField.placeholderText", "BCH-XXXX");
        JPanel batchP = createFieldPanel("Batch Number", batchField);

        unitField = new JTextField();
        unitField.setFont(ThemeConstants.fontBodyMd());
        unitField.putClientProperty("JTextField.placeholderText", "Tablet / Kapsul / Botol");
        JPanel unitP = createFieldPanel("Satuan", unitField);

        // Qty Counter
        JPanel qtyCounterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        qtyCounterPanel.setOpaque(false);
        qtyCounterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));

        JButton decBtn = new JButton("-");
        decBtn.setFont(ThemeConstants.fontTitleMd());
        decBtn.setPreferredSize(new Dimension(32, 32));
        decBtn.putClientProperty("FlatLaf.styleClass", "outline");
        decBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        qtyField = new JTextField("1");
        qtyField.setHorizontalAlignment(JTextField.CENTER);
        qtyField.setFont(ThemeConstants.fontBodyMd());
        qtyField.setPreferredSize(new Dimension(50, 32));

        JButton incBtn = new JButton("+");
        incBtn.setFont(ThemeConstants.fontTitleMd());
        incBtn.setPreferredSize(new Dimension(32, 32));
        incBtn.putClientProperty("FlatLaf.styleClass", "outline");
        incBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel unitLbl = new JLabel("Unit / Box");
        unitLbl.setFont(ThemeConstants.fontBodySm());
        unitLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        decBtn.addActionListener(e -> {
            try {
                int val = Integer.parseInt(qtyField.getText().trim());
                if (val > 0) qtyField.setText(String.valueOf(val - 1));
            } catch (NumberFormatException ignored) {}
        });

        incBtn.addActionListener(e -> {
            try {
                int val = Integer.parseInt(qtyField.getText().trim());
                qtyField.setText(String.valueOf(val + 1));
            } catch (NumberFormatException ignored) {}
        });

        qtyCounterPanel.add(decBtn);
        qtyCounterPanel.add(qtyField);
        qtyCounterPanel.add(incBtn);
        qtyCounterPanel.add(Box.createHorizontalStrut(8));
        qtyCounterPanel.add(unitLbl);
        JPanel qtyP = createFieldPanel("Jumlah Stok", qtyCounterPanel);

        minStockField = new JTextField();
        minStockField.setFont(ThemeConstants.fontBodyMd());
        minStockField.putClientProperty("JTextField.placeholderText", "10");
        JPanel minStockP = createFieldPanel("Minimum Stok", minStockField);

        purchasePriceField = new JTextField();
        purchasePriceField.setFont(ThemeConstants.fontBodyMd());
        purchasePriceField.putClientProperty("JTextField.placeholderText", "Contoh: 5000");
        JPanel buyP = createFieldPanel("Harga Beli (Rp)", purchasePriceField);

        sellingPriceField = new JTextField();
        sellingPriceField.setFont(ThemeConstants.fontBodyMd());
        sellingPriceField.putClientProperty("JTextField.placeholderText", "Contoh: 7500");
        JPanel sellP = createFieldPanel("Harga Jual (Rp)", sellingPriceField);

        notesArea = new JTextArea();
        notesArea.setFont(ThemeConstants.fontBodyMd());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setRows(2);
        notesArea.putClientProperty("JTextField.placeholderText", "Tambahkan instruksi penyimpanan atau catatan khusus...");
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER, 1));
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JPanel notesP = createFieldPanel("Catatan (Opsional)", notesScroll);

        // Grid Rows composition
        rightPanel.add(createRowPanel(nameP, codeP));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(createRowPanel(catP, expP));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(createRowPanel(batchP, unitP));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(createRowPanel(qtyP, minStockP));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(createRowPanel(buyP, sellP));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(notesP);
        rightPanel.add(Box.createVerticalStrut(14));

        // Form action buttons row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = new JButton("Batal");
        cancelBtn.setFont(ThemeConstants.fontTitleMd());
        cancelBtn.putClientProperty("FlatLaf.styleClass", "outline");
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Simpan Stok");
        saveBtn.setFont(ThemeConstants.fontTitleMd());
        saveBtn.putClientProperty("FlatLaf.styleClass", "primary");
        saveBtn.setPreferredSize(new Dimension(140, 36));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveMedicine());

        actionRow.add(cancelBtn);
        actionRow.add(saveBtn);
        rightPanel.add(actionRow);

        splitPanel.add(rightPanel, gbc);

        // Wrap the main interactive panel inside a JScrollPane to make the form fully responsive and scrollable
        JScrollPane splitScroll = new JScrollPane(splitPanel);
        splitScroll.setBorder(null);
        splitScroll.setOpaque(false);
        splitScroll.getViewport().setOpaque(false);
        splitScroll.getVerticalScrollBar().setUnitIncrement(16);
        splitScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(splitScroll, BorderLayout.CENTER);

        // 3. BOTTOM PANEL (Panduan Penginputan)
        RoundedPanel infoCard = new RoundedPanel(ThemeConstants.RADIUS_LG);
        infoCard.setBackground(Color.decode("#EFF6FF")); // Soft blue banner
        infoCard.setBorderColor(Color.decode("#BFDBFE"));
        infoCard.setLayout(new BorderLayout(12, 0));
        infoCard.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel infoIcon = new JLabel("Info");
        infoIcon.setFont(ThemeConstants.fontLabelSm());
        infoIcon.setForeground(Color.decode("#3B82F6"));
        infoCard.add(infoIcon, BorderLayout.WEST);

        JLabel infoText = new JLabel("<html><b>Panduan Penginputan:</b> Pastikan tanggal kedaluwarsa sesuai dengan fisik kemasan. Sistem akan memberikan notifikasi otomatis 3 bulan sebelum masa berlaku berakhir.</html>");
        infoText.setFont(ThemeConstants.fontBodySm());
        infoText.setForeground(Color.decode("#1E3A8A"));
        infoCard.add(infoText, BorderLayout.CENTER);

        container.add(infoCard, BorderLayout.SOUTH);

        setContentPane(container);
    }

    private JPanel createFieldPanel(String labelText, Component fieldComponent) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeConstants.fontLabelMd());
        lbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(fieldComponent);
        return p;
    }

    private JPanel createRowPanel(Component left, Component right) {
        JPanel r = new JPanel(new GridLayout(1, 2, 16, 0));
        r.setOpaque(false);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        r.add(left);
        r.add(right);
        return r;
    }

    private void updateBarcodePreview(String code) {
        previewPanel.removeAll();
        if (code != null && !code.isEmpty()) {
            try {
                BufferedImage barcodeImg = controller.generateBarcode(code);
                if (barcodeImg != null) {
                    // Rescale to fit the preview area nicely (e.g., width 180, height 50)
                    Image scaled = barcodeImg.getScaledInstance(180, 50, Image.SCALE_SMOOTH);
                    JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                    imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel codeLabel = new JLabel(code);
                    codeLabel.setFont(ThemeConstants.fontLabelSm());
                    codeLabel.setForeground(Color.decode("#1E40AF"));
                    codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JPanel wrap = new JPanel();
                    wrap.setOpaque(false);
                    wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
                    wrap.add(Box.createVerticalGlue());
                    wrap.add(imgLabel);
                    wrap.add(Box.createVerticalStrut(8));
                    wrap.add(codeLabel);
                    wrap.add(Box.createVerticalGlue());

                    previewPanel.add(wrap, BorderLayout.CENTER);
                } else {
                    previewPanel.add(scannerVisual, BorderLayout.CENTER);
                }
            } catch (Exception ex) {
                previewPanel.add(scannerVisual, BorderLayout.CENTER);
            }
        } else {
            previewPanel.add(scannerVisual, BorderLayout.CENTER);
        }
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void populateFields() {
        if (editMedicine == null) return;
        codeField.setText(editMedicine.getMedicineCode());
        nameField.setText(editMedicine.getMedicineName());
        categoryCombo.setSelectedItem(editMedicine.getCategory());
        batchField.setText(editMedicine.getBatchNumber());
        unitField.setText(editMedicine.getUnit());
        qtyField.setText(String.valueOf(editMedicine.getStockQuantity()));
        if (editMedicine.getExpiryDate() != null) {
            expiryField.setText(editMedicine.getExpiryDate().toString());
        }
        purchasePriceField.setText(editMedicine.getPurchasePrice().toPlainString());
        sellingPriceField.setText(editMedicine.getSellingPrice().toPlainString());
        minStockField.setText(String.valueOf(editMedicine.getMinimumStock()));
    }

    private void fillFromMedicine(Medicine med) {
        nameField.setText(med.getMedicineName());
        categoryCombo.setSelectedItem(med.getCategory());
        unitField.setText(med.getUnit());
        purchasePriceField.setText(med.getPurchasePrice().toPlainString());
        sellingPriceField.setText(med.getSellingPrice().toPlainString());
        minStockField.setText(String.valueOf(med.getMinimumStock()));
    }

    private void saveMedicine() {
        try {
            Medicine med = editMedicine != null ? editMedicine : new Medicine();
            med.setMedicineCode(codeField.getText().trim());
            med.setMedicineName(nameField.getText().trim());
            med.setCategory((String) categoryCombo.getSelectedItem());
            med.setBatchNumber(batchField.getText().trim());
            med.setUnit(unitField.getText().trim());

            int qty = 1;
            try {
                qty = Integer.parseInt(qtyField.getText().trim());
            } catch (NumberFormatException ignored) {}
            med.setStockQuantity(qty);

            if (!expiryField.getText().isBlank()) {
                med.setExpiryDate(LocalDate.parse(expiryField.getText().trim()));
            }
            if (!purchasePriceField.getText().isBlank()) {
                med.setPurchasePrice(new BigDecimal(purchasePriceField.getText().trim()));
            }
            if (!sellingPriceField.getText().isBlank()) {
                med.setSellingPrice(new BigDecimal(sellingPriceField.getText().trim()));
            }
            if (!minStockField.getText().isBlank()) {
                med.setMinimumStock(Integer.parseInt(minStockField.getText().trim()));
            }

            if (med.getMedicineName().isBlank()) {
                JOptionPane.showMessageDialog(this, "Nama obat wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (med.getMedicineCode().isBlank()) {
                JOptionPane.showMessageDialog(this, "Kode obat wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (editMedicine != null) {
                controller.updateMedicine(med);
            } else {
                controller.addMedicine(med);
            }
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// Inner helper class for rendering the scanner visual circle
class ScannerVisualPanel extends JPanel {
    public ScannerVisualPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(150, 150));
        setMaximumSize(new Dimension(150, 150));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = 120;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        // Draw dashed circle
        float[] dash = {6f, 4f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2.setColor(Color.decode("#3B82F6")); // Blue border
        g2.drawOval(x, y, size, size);

        // Draw scanner barcode inside
        int bx = x + size / 4;
        int by = y + size / 3;
        int bw = size / 2;
        int bh = size / 3;

        g2.setColor(Color.decode("#1E3A8A")); // Dark blue bars
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(bx, by, bx, by + bh);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(bx + 6, by, bx + 6, by + bh);
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(bx + 12, by, bx + 12, by + bh);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(bx + 20, by, bx + 20, by + bh);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(bx + 26, by, bx + 26, by + bh);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(bx + 32, by, bx + 32, by + bh);
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(bx + 40, by, bx + 40, by + bh);

        // Draw a horizontal red scanner line in middle
        g2.setColor(Color.decode("#EF4444")); // Red laser line
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(bx - 8, by + bh / 2, bx + bw + 8, by + bh / 2);

        g2.dispose();
    }
}
