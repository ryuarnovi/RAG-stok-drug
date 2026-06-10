package com.pharmastock.view.profile;

import com.pharmastock.controller.LoginController;
import com.pharmastock.view.ThemeConstants;
import com.pharmastock.view.MainFrame;
import com.pharmastock.view.components.BasePanel;
import com.pharmastock.view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends BasePanel {

    private final LoginController loginController;

    public ProfilePanel(LoginController loginController) {
        this.loginController = loginController;
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

        JLabel title = new JLabel("Profil");
        title.setFont(ThemeConstants.fontHeadlineLg());
        title.setForeground(ThemeConstants.ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(24));

        // Profile card
        RoundedPanel card = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        card.setBackground(ThemeConstants.SURFACE);
        card.setHasShadow(true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setMaximumSize(new Dimension(500, 300));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (loginController.getCurrentUser() != null) {
            var user = loginController.getCurrentUser();

            JLabel nameLabel = new JLabel(user.getFullName());
            nameLabel.setFont(ThemeConstants.fontHeadlineMd());
            nameLabel.setForeground(ThemeConstants.ON_SURFACE);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel usernameLabel = new JLabel("@" + user.getUsername());
            usernameLabel.setFont(ThemeConstants.fontBodyMd());
            usernameLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
            usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel roleLabel = new JLabel(user.getRole().name());
            roleLabel.setFont(ThemeConstants.fontLabelMd());
            roleLabel.setForeground(ThemeConstants.PRIMARY);
            roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(nameLabel);
            card.add(Box.createVerticalStrut(4));
            card.add(usernameLabel);
            card.add(Box.createVerticalStrut(8));
            card.add(roleLabel);
        }

        content.add(card);
        content.add(Box.createVerticalStrut(24));

        // Change password section
        RoundedPanel passCard = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        passCard.setBackground(ThemeConstants.SURFACE);
        passCard.setHasShadow(true);
        passCard.setLayout(new BoxLayout(passCard, BoxLayout.Y_AXIS));
        passCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        passCard.setMaximumSize(new Dimension(500, 280));
        passCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passTitle = new JLabel("Ganti Password");
        passTitle.setFont(ThemeConstants.fontTitleMd());
        passTitle.setForeground(ThemeConstants.ON_SURFACE);
        passTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField oldPass = new JPasswordField();
        oldPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        oldPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        oldPass.putClientProperty("JTextField.placeholderText", "Password lama");

        JPasswordField newPass = new JPasswordField();
        newPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        newPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPass.putClientProperty("JTextField.placeholderText", "Password baru");

        JButton changeBtn = new JButton("Ubah Password");
        changeBtn.setFont(ThemeConstants.fontTitleMd());
        changeBtn.putClientProperty("FlatLaf.styleClass", "primary");
        changeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ThemeConstants.TOUCH_TARGET));
        changeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeBtn.addActionListener(e -> {
            try {
                loginController.changePassword(
                        new String(oldPass.getPassword()),
                        new String(newPass.getPassword()));
                JOptionPane.showMessageDialog(this, "Password berhasil diubah.");
                oldPass.setText("");
                newPass.setText("");
            } catch (LoginController.AuthenticationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        passCard.add(passTitle);
        passCard.add(Box.createVerticalStrut(12));
        passCard.add(oldPass);
        passCard.add(Box.createVerticalStrut(8));
        passCard.add(newPass);
        passCard.add(Box.createVerticalStrut(16));
        passCard.add(changeBtn);
        content.add(passCard);
        content.add(Box.createVerticalStrut(24));

        // Logout
        JButton logoutBtn = new JButton("Keluar");
        logoutBtn.setFont(ThemeConstants.fontTitleMd());
        logoutBtn.putClientProperty("FlatLaf.styleClass", "danger");
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(500, ThemeConstants.TOUCH_TARGET));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame mainFrame) {
                mainFrame.triggerLogout();
            } else {
                loginController.logout();
                System.exit(0);
            }
        });
        content.add(logoutBtn);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        // No data to reload on profile panel
    }
}
