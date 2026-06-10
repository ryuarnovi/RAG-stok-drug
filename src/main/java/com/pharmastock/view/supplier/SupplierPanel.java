package com.pharmastock.view.supplier;

import com.pharmastock.controller.SupplierController;
import com.pharmastock.model.Supplier;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.components.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierPanel extends BasePanel {

    private final SupplierController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    public SupplierPanel(SupplierController controller) {
        this.controller = controller;
        setBackground(ThemeConstants.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        initUI();
        refreshData();
    }

    @Override
    protected void initUI() {
        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel title = new JLabel("Supplier Management");
        title.setFont(ThemeConstants.fontHeadlineLg());
        title.setForeground(ThemeConstants.ON_SURFACE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JTextField searchField = new JTextField(20);
        searchField.setFont(ThemeConstants.fontBodyMd());
        searchField.putClientProperty("JTextField.placeholderText", "Cari supplier...");
        searchField.addActionListener(e -> {
            List<Supplier> results = controller.searchSuppliers(searchField.getText().trim());
            updateTable(results);
        });

        JButton addBtn = new JButton("+ Tambah Supplier");
        addBtn.setFont(ThemeConstants.fontTitleMd());
        addBtn.putClientProperty("FlatLaf.styleClass", "primary");
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddDialog());

        btnPanel.add(searchField);
        btnPanel.add(addBtn);
        headerRow.add(title, BorderLayout.WEST);
        headerRow.add(btnPanel, BorderLayout.EAST);
        add(headerRow, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Nama Supplier", "Contact Person", "Telepon", "Email", "Alamat"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(ThemeConstants.fontBodyMd());
        table.setRowHeight(40);
        table.getTableHeader().setFont(ThemeConstants.fontLabelMd());
        table.getTableHeader().setBackground(ThemeConstants.SURFACE_CONTAINER_LOW);
        table.setSelectionBackground(ThemeConstants.PRIMARY_TINT_10);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottomPanel.setOpaque(false);

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(ThemeConstants.fontBodyMd());
        editBtn.putClientProperty("FlatLaf.styleClass", "outline");
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> editSelected());

        JButton deleteBtn = new JButton("Hapus");
        deleteBtn.setFont(ThemeConstants.fontBodyMd());
        deleteBtn.putClientProperty("FlatLaf.styleClass", "danger");
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteSelected());

        bottomPanel.add(editBtn);
        bottomPanel.add(deleteBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        updateTable(controller.getAllSuppliers());
    }

    private void updateTable(List<Supplier> suppliers) {
        tableModel.setRowCount(0);
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                    s.getSupplierId(), s.getSupplierName(), s.getContactPerson(),
                    s.getPhone(), s.getEmail(), s.getAddress()
            });
        }
    }

    private void showAddDialog() {
        SupplierDialog dialog = new SupplierDialog(SwingUtilities.getWindowAncestor(this), controller, null);
        dialog.setVisible(true);
        refreshData();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih supplier terlebih dahulu."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        controller.getSupplierById(id).ifPresent(s -> {
            SupplierDialog dialog = new SupplierDialog(SwingUtilities.getWindowAncestor(this), controller, s);
            dialog.setVisible(true);
            refreshData();
        });
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih supplier terlebih dahulu."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus supplier " + name + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteSupplier(id);
                refreshData();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Tidak Dapat Menghapus", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
