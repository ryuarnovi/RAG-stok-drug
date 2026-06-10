package com.pharmastock.view;

import com.pharmastock.controller.LoginController;
import com.pharmastock.view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginView extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private final LoginController loginController;
    private Runnable onLoginSuccess;

    public LoginView(LoginController loginController) {
        this.loginController = loginController;
        setBackground(ThemeConstants.BACKGROUND);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        RoundedPanel card = new RoundedPanel(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        card.setBackground(ThemeConstants.SURFACE);
        card.setHasShadow(true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(420, 460));

        // Logo
        JLabel logo = new JLabel("PharmaStock");
        logo.setFont(ThemeConstants.fontHeadlineLg());
        logo.setForeground(ThemeConstants.PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Pharmacy Inventory Management");
        subtitle.setFont(ThemeConstants.fontBodyMd());
        subtitle.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeConstants.fontBodySm());
        errorLabel.setForeground(ThemeConstants.DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username block
        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setMaximumSize(new Dimension(340, 70));
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeConstants.fontLabelMd());
        userLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(ThemeConstants.fontBodyMd());
        usernameField.setMaximumSize(new Dimension(340, ThemeConstants.TOUCH_TARGET));
        usernameField.setPreferredSize(new Dimension(340, ThemeConstants.TOUCH_TARGET));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.putClientProperty("JTextField.placeholderText", "Masukkan username");
        
        userPanel.add(userLabel);
        Component userStrut = Box.createVerticalStrut(6);
        if (userStrut instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        userPanel.add(userStrut);
        userPanel.add(usernameField);

        // Password block
        JPanel passPanel = new JPanel();
        passPanel.setOpaque(false);
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.Y_AXIS));
        passPanel.setMaximumSize(new Dimension(340, 70));
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeConstants.fontLabelMd());
        passLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(ThemeConstants.fontBodyMd());
        passwordField.setMaximumSize(new Dimension(340, ThemeConstants.TOUCH_TARGET));
        passwordField.setPreferredSize(new Dimension(340, ThemeConstants.TOUCH_TARGET));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.putClientProperty("JTextField.placeholderText", "Masukkan password");
        
        passPanel.add(passLabel);
        Component passStrut = Box.createVerticalStrut(6);
        if (passStrut instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        passPanel.add(passStrut);
        passPanel.add(passwordField);

        // Login button
        loginButton = new JButton("Masuk");
        loginButton.setFont(ThemeConstants.fontTitleMd());
        loginButton.putClientProperty("FlatLaf.styleClass", "primary");
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(340, ThemeConstants.TOUCH_TARGET));
        loginButton.setPreferredSize(new Dimension(340, ThemeConstants.TOUCH_TARGET));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> performLogin());
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });

        // Info
        JLabel infoLabel = new JLabel("Default: admin / admin123");
        infoLabel.setFont(ThemeConstants.fontBodySm());
        infoLabel.setForeground(ThemeConstants.OUTLINE);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(userPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(passPanel);
        card.add(Box.createVerticalStrut(24));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(16));
        card.add(infoLabel);

        add(card);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        errorLabel.setText(" ");

        try {
            loginController.login(username, password);
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } catch (LoginController.AuthenticationException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
        }
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        usernameField.requestFocus();
    }
}
