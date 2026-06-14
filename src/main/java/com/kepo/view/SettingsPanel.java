package com.kepo.view;

import com.kepo.model.User;
import com.kepo.service.UserService;
import com.kepo.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.stream.Collectors;

public class SettingsPanel extends VBox implements RefreshablePanel {

    private final UserService userService;
    private final MainLayout mainLayout;

    private FlowPane cardsGrid;
    private TextField searchField;

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField fullNameField;
    private ComboBox<User.Role> roleCombo;
    private Label errorLabel;
    private Button deleteBtn;

    // Side Drawer Simulation
    private VBox drawer;
    private Label drawerTitle;

    private User selectedUser;

    public SettingsPanel(UserService userService, MainLayout mainLayout) {
        this.userService = userService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        // --- Header Row ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Pengaturan Sistem & Manajemen Pengguna");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Cari operator...");
        searchField.setStyle(ThemeConstants.INPUT_STYLE);
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards(newVal));

        boolean isAdmin = userService.getCurrentUser() != null && userService.getCurrentUser().getRole() == User.Role.ADMIN;

        Button newUserBtn = new Button("Tambah Operator");
        newUserBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newUserBtn.setOnAction(e -> openDrawerForCreate());
        newUserBtn.setVisible(isAdmin);

        header.getChildren().addAll(title, spacer, searchField, newUserBtn);
        getChildren().add(header);

        // --- Body Split Layout ---
        HBox body = new HBox(20);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Left Cards Grid Scroll Area
        cardsGrid = new FlowPane();
        cardsGrid.setHgap(16);
        cardsGrid.setVgap(16);
        cardsGrid.setPadding(new Insets(2));

        ScrollPane gridScroll = new ScrollPane(cardsGrid);
        HBox.setHgrow(gridScroll, Priority.ALWAYS);
        gridScroll.setFitToWidth(true);
        gridScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Right Drawer Panel
        drawer = new VBox(15);
        drawer.setPrefWidth(350);
        drawer.setStyle(ThemeConstants.CARD_STYLE);
        drawer.setPadding(new Insets(20));

        if (isAdmin) {
            drawerTitle = new Label("Tambah / Edit Pengguna");
            drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
            drawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

            usernameField = new TextField();
            usernameField.setPromptText("Username login");
            usernameField.setStyle(ThemeConstants.INPUT_STYLE);

            passwordField = new PasswordField();
            passwordField.setPromptText("Kosongkan jika tidak diubah");
            passwordField.setStyle(ThemeConstants.INPUT_STYLE);

            fullNameField = new TextField();
            fullNameField.setPromptText("Nama lengkap operator");
            fullNameField.setStyle(ThemeConstants.INPUT_STYLE);

            roleCombo = new ComboBox<>(FXCollections.observableArrayList(User.Role.values()));
            roleCombo.setValue(User.Role.SHELTER_OFFICER);
            roleCombo.setMaxWidth(Double.MAX_VALUE);
            roleCombo.setStyle(ThemeConstants.INPUT_STYLE);

            errorLabel = new Label();
            errorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

            HBox btnRow = new HBox(10);
            Button saveBtn = new Button("Simpan");
            saveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
            saveBtn.setOnAction(e -> handleSave());

            Button resetBtn = new Button("Tutup");
            resetBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
            resetBtn.setOnAction(e -> closeDrawer());

            deleteBtn = new Button("Hapus");
            deleteBtn.setStyle(ThemeConstants.DANGER_BTN_STYLE);
            deleteBtn.setOnAction(e -> handleDelete());

            btnRow.getChildren().addAll(saveBtn, resetBtn, deleteBtn);

            drawer.getChildren().addAll(
                    drawerTitle, new Separator(),
                    createFormLabel("Username"), usernameField, 
                    createFormLabel("Password"), passwordField, 
                    createFormLabel("Nama Lengkap"), fullNameField, 
                    createFormLabel("Hak Akses (Role)"), roleCombo, 
                    errorLabel, btnRow
            );
        } else {
            Label infoTitle = new Label("Informasi Hak Akses");
            infoTitle.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 15px;");
            
            Label infoDesc = new Label("Anda saat ini masuk sebagai operator non-admin. Pembuatan akun baru, penyuntingan hak akses, dan penghapusan pengguna hanya dapat dilakukan oleh akun Administrator.");
            infoDesc.setWrapText(true);
            infoDesc.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");

            drawer.getChildren().addAll(infoTitle, infoDesc);
        }

        closeDrawer();

        body.getChildren().addAll(gridScroll, drawer);
        getChildren().add(body);
        
        refreshData();
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        return label;
    }

    private void renderCards(String query) {
        cardsGrid.getChildren().clear();
        List<User> list = userService.getAllUsers();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            list = list.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(q) ||
                            u.getFullName().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        boolean isAdmin = userService.getCurrentUser() != null && userService.getCurrentUser().getRole() == User.Role.ADMIN;

        for (User u : list) {
            VBox card = new VBox(10);
            card.setPrefWidth(240);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // Full Name
            Label nameLabel = new Label(u.getFullName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 15px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(208);
            nameLabel.setPrefWidth(208);

            // Username
            Label userLabel = new Label("ID Pengguna: " + u.getUsername());
            userLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");

            // Role Badge
            Label badge = new Label(u.getRole().name());
            if (u.getRole() == User.Role.ADMIN) {
                badge.setStyle(ThemeConstants.BADGE_CRITICAL);
            } else if (u.getRole() == User.Role.SHELTER_OFFICER) {
                badge.setStyle(ThemeConstants.BADGE_ACTIVE);
            } else {
                badge.setStyle(ThemeConstants.BADGE_SAFE);
            }

            HBox badgeRow = new HBox(badge);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            // Action details button
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + " -fx-padding: 4 12 4 12;");
            
            if (isAdmin) {
                detailsBtn.setOnAction(evt -> openDrawerForEdit(u));
            } else {
                detailsBtn.setDisable(true);
            }

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, userLabel, badgeRow, footer);
            cardsGrid.getChildren().add(card);
        }
    }

    private void openDrawerForCreate() {
        selectedUser = null;
        if (drawerTitle != null) drawerTitle.setText("Registrasi Pengguna Baru");

        if (usernameField != null) {
            usernameField.clear();
            usernameField.setDisable(false);
            fullNameField.clear();
            roleCombo.setValue(User.Role.SHELTER_OFFICER);
            passwordField.clear();
            deleteBtn.setVisible(false);
            errorLabel.setText("");
        }

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void openDrawerForEdit(User u) {
        selectedUser = u;
        if (drawerTitle != null) drawerTitle.setText("Edit Data Pengguna");

        if (usernameField != null) {
            usernameField.setText(u.getUsername());
            usernameField.setDisable(true); // Cannot change username
            fullNameField.setText(u.getFullName());
            roleCombo.setValue(u.getRole());
            passwordField.clear();
            deleteBtn.setVisible(true);
            errorLabel.setText("");
        }

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void closeDrawer() {
        drawer.setVisible(false);
        drawer.setManaged(false);
        selectedUser = null;
    }

    private void handleSave() {
        if (usernameField == null) return;
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String name = fullNameField.getText().trim();
        User.Role role = roleCombo.getValue();

        if (username.isEmpty() || name.isEmpty()) {
            errorLabel.setText("Username dan Nama Lengkap wajib diisi.");
            return;
        }

        User u = selectedUser;
        if (u == null) {
            if (password.isEmpty()) {
                errorLabel.setText("Password wajib diisi untuk pengguna baru.");
                return;
            }
            u = new User();
            u.setUsername(username);
            u.setPasswordHash(PasswordUtil.hash(password));
        } else {
            if (!password.isEmpty()) {
                u.setPasswordHash(PasswordUtil.hash(password));
            }
        }
        
        u.setFullName(name);
        u.setRole(role);

        if (userService.saveUser(u)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyimpan pengguna.");
        }
    }

    private void handleDelete() {
        if (selectedUser == null) {
            errorLabel.setText("Pilih pengguna yang ingin dihapus.");
            return;
        }
        if (selectedUser.getUsername().equals(userService.getCurrentUser().getUsername())) {
            errorLabel.setText("Anda tidak dapat menghapus akun Anda sendiri.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus pengguna " + selectedUser.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (userService.deleteUser(selectedUser.getUserId())) {
                    closeDrawer();
                    refreshData();
                } else {
                    errorLabel.setText("Gagal menghapus pengguna.");
                }
            }
        });
    }

    @Override
    public void refreshData() {
        // Refresh operator list
        renderCards(searchField != null ? searchField.getText() : null);
    }
}
