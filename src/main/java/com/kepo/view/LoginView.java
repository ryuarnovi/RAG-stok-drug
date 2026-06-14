package com.kepo.view;

import com.kepo.KepoApp;
import com.kepo.controller.LoginController;
import com.kepo.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LoginView extends HBox {

    private final LoginController controller;
    private final KepoApp app;

    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView(LoginController controller, KepoApp app) {
        this.controller = controller;
        this.app = app;
        
        initUI();
    }

    private void initUI() {
        // --- Left Pane (Hero Branding) ---
        VBox heroPane = new VBox(20);
        heroPane.setPrefWidth(450);
        heroPane.setPadding(new Insets(40));
        heroPane.setAlignment(Pos.CENTER_LEFT);
        heroPane.setStyle("-fx-background-color: linear-gradient(to bottom, #004c6e, " + ThemeConstants.PRIMARY + ");");
        
        Text logoText = new Text("KEPO");
        logoText.setFont(Font.font("System", FontWeight.BLACK, 40));
        logoText.setFill(Color.WHITE);
        
        Text subLogoText = new Text("Kendali Evakuasi & Pengelolaan Operasional Bencana");
        subLogoText.setFont(Font.font("System", FontWeight.BOLD, 18));
        subLogoText.setFill(Color.web("#e2e8f0"));
        subLogoText.setWrappingWidth(370);
        
        Text descText = new Text("Sistem informasi terintegrasi pusat komando (Command Center) penanggulangan bencana kebakaran secara real-time. Kelola shelter, pantau kapasitas pengungsi, monitor logistik obat, dan kelola distribusi bantuan.");
        descText.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descText.setFill(Color.web("#94a3b8"));
        descText.setWrappingWidth(370);
        
        heroPane.getChildren().addAll(logoText, subLogoText, descText);

        // --- Right Pane (Login Form) ---
        VBox formPane = new VBox(20);
        formPane.setPrefWidth(450);
        formPane.setPadding(new Insets(60, 40, 60, 40));
        formPane.setAlignment(Pos.CENTER_LEFT);
        formPane.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND + ";");

        Label welcomeLabel = new Label("Selamat Datang");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Label subWelcomeLabel = new Label("Silakan masuk menggunakan kredensial Anda");
        subWelcomeLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subWelcomeLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));

        // Form Fields Container
        VBox fieldsBox = new VBox(15);
        fieldsBox.setAlignment(Pos.CENTER_LEFT);

        VBox userBox = new VBox(5);
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        userLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
        usernameField = new TextField();
        usernameField.setPromptText("Masukkan username Anda");
        usernameField.setPrefHeight(40);
        usernameField.setStyle(ThemeConstants.INPUT_STYLE);
        userBox.getChildren().addAll(userLabel, usernameField);

        VBox passBox = new VBox(5);
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        passLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
        passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password Anda");
        passwordField.setPrefHeight(40);
        passwordField.setStyle(ThemeConstants.INPUT_STYLE);
        passBox.getChildren().addAll(passLabel, passwordField);

        errorLabel = new Label("");
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        errorLabel.setTextFill(Color.web(ThemeConstants.DANGER));
        errorLabel.setWrapText(true);

        loginButton = new Button("MASUK");
        loginButton.setPrefHeight(44);
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        loginButton.setOnAction(e -> handleLogin());

        // Default credentials hint
        Label hintLabel = new Label("Kredensial Demo:\n- Admin: admin / admin123\n- Health: health / pharma123\n- Shelter: shelter / staff123");
        hintLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        hintLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE_VARIANT));
        hintLabel.setPadding(new Insets(10, 0, 0, 0));

        fieldsBox.getChildren().addAll(userBox, passBox, errorLabel, loginButton, hintLabel);

        formPane.getChildren().addAll(welcomeLabel, subWelcomeLabel, fieldsBox);

        // Assemble splits
        this.getChildren().addAll(heroPane, formPane);
        HBox.setHgrow(heroPane, Priority.ALWAYS);
        HBox.setHgrow(formPane, Priority.ALWAYS);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        errorLabel.setText("");
        loginButton.setDisable(true);
        loginButton.setText("MEMPROSES...");

        try {
            User user = controller.login(username, password);
            if (user != null) {
                app.showMainApplication();
            }
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
            loginButton.setDisable(false);
            loginButton.setText("MASUK");
        }
    }
}
