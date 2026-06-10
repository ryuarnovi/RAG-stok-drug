package com.pharmastock.view.supplier;

import com.pharmastock.controller.SupplierController;
import com.pharmastock.model.Supplier;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.components.BaseDialog;

import javax.swing.*;
import java.awt.*;

public class SupplierDialog extends BaseDialog {

    private final SupplierController controller;
    private final Supplier editSupplier;
    private JTextField nameField, contactField, phoneField, emailField;
    private JTextArea addressArea;

    public SupplierDialog(Window owner, SupplierController controller, Supplier editSupplier) {
        super(owner, editSupplier != null ? "Edit Supplier" : "Tambah Supplier", true);
        this.controller = controller;
        this.editSupplier = editSupplier;
        setSize(450, 500);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ThemeConstants.SURFACE);
        // initUI is called automatically by subclass or here. Wait! JDialog constructor super() does NOT call initUI(). We must call it manually in the constructor!
        initUI();
        if (editSupplier != null) populateFields();
    }

    @Override
    protected void initUI() {
        JPanel content = new JPanel();
        content.setBackground(ThemeConstants.SURFACE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        nameField = addField(content, "Nama Supplier", "PT Example Farma");
        contactField = addField(content, "Contact Person", "Nama kontak");
        phoneField = addField(content, "Telepon", "021-XXXXXXX");
        emailField = addField(content, "Email", "email@example.com");

        JLabel addrLabel = new JLabel("Alamat");
        addrLabel.setFont(ThemeConstants.fontLabelMd());
        addrLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        addrLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(addrLabel);
        content.add(Box.createVerticalStrut(4));

        addressArea = new JTextArea(3, 20);
        addressArea.setFont(ThemeConstants.fontBodyMd());
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        addrScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        content.add(addrScroll);
        content.add(Box.createVerticalStrut(20));

        JButton saveBtn = new JButton("Simpan");
        saveBtn.setFont(ThemeConstants.fontTitleMd());
        saveBtn.putClientProperty("FlatLaf.styleClass", "primary");
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> save());

        JButton cancelBtn = new JButton("Batal");
        cancelBtn.setFont(ThemeConstants.fontTitleMd());
        cancelBtn.putClientProperty("FlatLaf.styleClass", "outline");
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());

        content.add(saveBtn);
        content.add(Box.createVerticalStrut(8));
        content.add(cancelBtn);

        setContentPane(content);
    }

    private JTextField addField(JPanel parent, String label, String placeholder) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeConstants.fontLabelMd());
        lbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));

        JTextField field = new JTextField();
        field.setFont(ThemeConstants.fontBodyMd());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
        return field;
    }

    private void populateFields() {
        nameField.setText(editSupplier.getSupplierName());
        contactField.setText(editSupplier.getContactPerson());
        phoneField.setText(editSupplier.getPhone());
        emailField.setText(editSupplier.getEmail());
        addressArea.setText(editSupplier.getAddress());
    }

    private void save() {
        try {
            Supplier s = editSupplier != null ? editSupplier : new Supplier();
            s.setSupplierName(nameField.getText().trim());
            s.setContactPerson(contactField.getText().trim());
            s.setPhone(phoneField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setAddress(addressArea.getText().trim());

            if (editSupplier != null) {
                controller.updateSupplier(s);
            } else {
                controller.addSupplier(s);
            }
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }
}
