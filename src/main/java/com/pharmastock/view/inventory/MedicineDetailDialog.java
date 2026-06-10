package com.pharmastock.view.inventory;

import com.pharmastock.controller.InventoryController;
import com.pharmastock.model.Medicine;
import com.pharmastock.view.ThemeConstants;

import com.pharmastock.view.components.BaseDialog;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
public class MedicineDetailDialog extends BaseDialog {

    private final Medicine medicine;
    private final InventoryController controller;

    public MedicineDetailDialog(Window owner, Medicine medicine, InventoryController controller) {
        super(owner, "Detail Obat: " + medicine.getMedicineName(), true);
        this.medicine = medicine;
        this.controller = controller;
        setSize(520, 580);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ThemeConstants.SURFACE);
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel content = new JPanel();
        content.setBackground(ThemeConstants.SURFACE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JLabel title = new JLabel(medicine.getMedicineName());
        title.setFont(ThemeConstants.fontHeadlineLg());
        title.setForeground(ThemeConstants.ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel(medicine.getCategory() + " - " + medicine.getUnit());
        subtitle.setFont(ThemeConstants.fontBodyMd());
        subtitle.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(20));

        // Detail grid
        addDetailRow(content, "Kode Obat", medicine.getMedicineCode());
        addDetailRow(content, "Batch", medicine.getBatchNumber());
        addDetailRow(content, "Stok", String.format("%,d %s", medicine.getStockQuantity(), medicine.getUnit()));
        addDetailRow(content, "Minimum Stok", String.valueOf(medicine.getMinimumStock()));
        addDetailRow(content, "Harga Beli", String.format("Rp %,.0f", medicine.getPurchasePrice()));
        addDetailRow(content, "Harga Jual", String.format("Rp %,.0f", medicine.getSellingPrice()));
        addDetailRow(content, "Kadaluarsa",
                medicine.getExpiryDate() != null ? medicine.getExpiryDate().toString() : "-");
        addDetailRow(content, "Status", medicine.getStockStatusLabel());
        addDetailRow(content, "Supplier", medicine.getSupplierName() != null ? medicine.getSupplierName() : "-");

        content.add(Box.createVerticalStrut(16));

        // Barcode
        BufferedImage barcode = controller.generateBarcode(medicine.getMedicineCode());
        if (barcode != null) {
            JLabel barcodeLabel = new JLabel(new ImageIcon(barcode));
            barcodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(barcodeLabel);
            content.add(Box.createVerticalStrut(16));
        }

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(ThemeConstants.fontTitleMd());
        editBtn.putClientProperty("FlatLaf.styleClass", "primary");
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> {
            dispose();
            AddMedicineDialog dialog = new AddMedicineDialog(getOwner(), controller, medicine);
            dialog.setVisible(true);
        });

        JButton deleteBtn = new JButton("Hapus");
        deleteBtn.setFont(ThemeConstants.fontTitleMd());
        deleteBtn.putClientProperty("FlatLaf.styleClass", "danger");
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Hapus " + medicine.getMedicineName() + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.deleteMedicine(medicine.getMedicineId());
                dispose();
            }
        });

        JButton closeBtn = new JButton("Tutup");
        closeBtn.setFont(ThemeConstants.fontTitleMd());
        closeBtn.putClientProperty("FlatLaf.styleClass", "outline");
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        btnRow.add(editBtn);
        btnRow.add(deleteBtn);
        btnRow.add(closeBtn);
        content.add(btnRow);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scroll);
    }

    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(ThemeConstants.fontBodySm());
        labelLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        labelLbl.setPreferredSize(new Dimension(120, 20));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(ThemeConstants.fontBodyMd());
        valueLbl.setForeground(ThemeConstants.ON_SURFACE);

        row.add(labelLbl, BorderLayout.WEST);
        row.add(valueLbl, BorderLayout.CENTER);
        parent.add(row);
    }
}
