package com.kepo.view;

import com.kepo.model.Shelter;
import com.kepo.service.ShelterService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.stream.Collectors;

public class ShelterPanel extends VBox implements RefreshablePanel {

    private final ShelterService shelterService;
    private final MainLayout mainLayout;

    private TextField searchField;
    private FlowPane cardsGrid;

    // Right-hand Drawer
    private VBox drawer;
    private Label drawerTitle;
    private TextField nameField;
    private TextField locationField;
    private TextField capacityField;
    private TextField occupancyField;
    private TextField pjField;
    private Label errorLabel;
    private Button deleteBtn;

    private Shelter selectedShelter;

    public ShelterPanel(ShelterService shelterService, MainLayout mainLayout) {
        this.shelterService = shelterService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        // --- Header Section ---
        Label title = new Label("Manajemen Shelter Evakuasi");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        getChildren().add(title);

        // --- Search & Action Row ---
        HBox searchRow = new HBox(15);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Cari shelter...");
        searchField.setStyle(ThemeConstants.INPUT_STYLE);
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newShelterBtn = new Button("Tambah Shelter Baru");
        newShelterBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newShelterBtn.setOnAction(e -> openDrawerForCreate());

        searchRow.getChildren().addAll(searchField, spacer, newShelterBtn);
        getChildren().add(searchRow);

        // --- Main Split Content ---
        HBox mainBody = new HBox(20);
        VBox.setVgrow(mainBody, Priority.ALWAYS);

        // Left Cards Scroll Area
        cardsGrid = new FlowPane();
        cardsGrid.setHgap(16);
        cardsGrid.setVgap(16);
        cardsGrid.setPadding(new Insets(2));

        ScrollPane gridScroll = new ScrollPane(cardsGrid);
        HBox.setHgrow(gridScroll, Priority.ALWAYS);
        gridScroll.setFitToWidth(true);
        gridScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Right Drawer Panel
        drawer = new VBox(12);
        drawer.setPrefWidth(350);
        drawer.setPadding(new Insets(20));
        drawer.setStyle(ThemeConstants.CARD_STYLE);

        drawerTitle = new Label("Detail Shelter");
        drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        drawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        nameField = new TextField();
        nameField.setPromptText("Nama GOR / Masjid / Kantor");
        nameField.setStyle(ThemeConstants.INPUT_STYLE);

        locationField = new TextField();
        locationField.setPromptText("Alamat / Lokasi koordinat");
        locationField.setStyle(ThemeConstants.INPUT_STYLE);

        capacityField = new TextField();
        capacityField.setPromptText("Jumlah kapasitas daya tampung");
        capacityField.setStyle(ThemeConstants.INPUT_STYLE);

        occupancyField = new TextField();
        occupancyField.setPromptText("Jumlah pengungsi terisi");
        occupancyField.setStyle(ThemeConstants.INPUT_STYLE);

        pjField = new TextField();
        pjField.setPromptText("Nama penanggung jawab");
        pjField.setStyle(ThemeConstants.INPUT_STYLE);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

        HBox btnRow = new HBox(8);
        Button saveBtn = new Button("Simpan");
        saveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        saveBtn.setOnAction(e -> handleSave());

        Button cancelBtn = new Button("Tutup");
        cancelBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        cancelBtn.setOnAction(e -> closeDrawer());

        deleteBtn = new Button("Hapus");
        deleteBtn.setStyle(ThemeConstants.DANGER_BTN_STYLE);
        deleteBtn.setOnAction(e -> handleDelete());

        btnRow.getChildren().addAll(saveBtn, cancelBtn, deleteBtn);

        drawer.getChildren().addAll(
                drawerTitle,
                createFormLabel("Nama Shelter"), nameField,
                createFormLabel("Lokasi"), locationField,
                createFormLabel("Kapasitas (Orang)"), capacityField,
                createFormLabel("Terisi Sekarang"), occupancyField,
                createFormLabel("Penanggung Jawab"), pjField,
                errorLabel,
                btnRow
        );

        closeDrawer();

        mainBody.getChildren().addAll(gridScroll, drawer);
        getChildren().add(mainBody);

        refreshData();
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        return label;
    }

    private void renderCards(String query) {
        cardsGrid.getChildren().clear();
        List<Shelter> list = shelterService.getAllShelters();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            list = list.stream()
                    .filter(s -> s.getName().toLowerCase().contains(q) || s.getLocation().toLowerCase().contains(q) || s.getPenanggungJawab().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        for (Shelter s : list) {
            VBox card = new VBox(12);
            card.setPrefWidth(250);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // GOR Name
            Label nameLabel = new Label(s.getName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 15px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(218);
            nameLabel.setPrefWidth(218);

            // Location
            Label locLabel = new Label("Lokasi: " + s.getLocation());
            locLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");
            locLabel.setWrapText(true);
            locLabel.setMaxWidth(218);
            locLabel.setPrefWidth(218);

            // Penanggung Jawab
            Label pjLabel = new Label("PJ: " + s.getPenanggungJawab());
            pjLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            pjLabel.setWrapText(true);
            pjLabel.setMaxWidth(218);
            pjLabel.setPrefWidth(218);

            // Progress text bar (█████████░ 95%)
            double ratio = s.getCapacity() > 0 ? (double) s.getCurrentOccupancy() / s.getCapacity() : 0.0;
            String textBar = getCapacityBar(s.getCurrentOccupancy(), s.getCapacity());
            Label barLabel = new Label(textBar);
            barLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 12px;");

            // Occupancy ratio text
            Label statsLabel = new Label(s.getCurrentOccupancy() + " / " + s.getCapacity() + " Jiwa");
            statsLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

            // Severity Status Badge (SAFE, WARNING, CRITICAL)
            Label badge = new Label();
            if ("KRITIS".equals(s.getStatus()) || s.getCurrentOccupancy() >= s.getCapacity()) {
                badge.setText("CRITICAL");
                badge.setStyle(ThemeConstants.BADGE_CRITICAL);
            } else if ("WASPADA".equals(s.getStatus())) {
                badge.setText("WARNING");
                badge.setStyle(ThemeConstants.BADGE_WARNING);
            } else {
                badge.setText("SAFE");
                badge.setStyle(ThemeConstants.BADGE_SAFE);
            }

            HBox badgeRow = new HBox(badge);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            // Actions
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + "-fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(e -> openDrawerForEdit(s));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, locLabel, pjLabel, barLabel, statsLabel, badgeRow, footer);
            cardsGrid.getChildren().add(card);
        }
    }

    private String getCapacityBar(int current, int capacity) {
        if (capacity <= 0) return "░░░░░░░░░░ 0%";
        double pct = (double) current / capacity;
        int filledChars = (int) Math.round(pct * 10);
        if (filledChars > 10) filledChars = 10;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < filledChars) {
                sb.append("█");
            } else {
                sb.append("░");
            }
        }
        sb.append(String.format(" %d%%", (int)(pct * 100)));
        return sb.toString();
    }

    private void openDrawerForCreate() {
        selectedShelter = null;
        drawerTitle.setText("Registrasi Shelter Baru");
        nameField.clear();
        locationField.clear();
        capacityField.clear();
        occupancyField.clear();
        pjField.clear();
        errorLabel.setText("");
        deleteBtn.setVisible(false);

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void openDrawerForEdit(Shelter s) {
        selectedShelter = s;
        drawerTitle.setText("Edit Detail GOR Shelter");
        nameField.setText(s.getName());
        locationField.setText(s.getLocation());
        capacityField.setText(String.valueOf(s.getCapacity()));
        occupancyField.setText(String.valueOf(s.getCurrentOccupancy()));
        pjField.setText(s.getPenanggungJawab());
        errorLabel.setText("");
        deleteBtn.setVisible(true);

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void closeDrawer() {
        drawer.setVisible(false);
        drawer.setManaged(false);
        selectedShelter = null;
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        String capStr = capacityField.getText().trim();
        String occStr = occupancyField.getText().trim();
        String pj = pjField.getText().trim();

        if (name.isEmpty() || location.isEmpty() || capStr.isEmpty() || occStr.isEmpty() || pj.isEmpty()) {
            errorLabel.setText("Semua kolom harus diisi.");
            return;
        }

        int capacity;
        int occupancy;
        try {
            capacity = Integer.parseInt(capStr);
            occupancy = Integer.parseInt(occStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Kapasitas & keterisian harus berupa angka.");
            return;
        }

        Shelter s = selectedShelter;
        if (s == null) {
            s = new Shelter();
        }
        s.setName(name);
        s.setLocation(location);
        s.setCapacity(capacity);
        s.setCurrentOccupancy(occupancy);
        s.setPenanggungJawab(pj);

        if (shelterService.saveShelter(s)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyimpan data shelter.");
        }
    }

    private void handleDelete() {
        if (selectedShelter == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus shelter " + selectedShelter.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (shelterService.deleteShelter(selectedShelter.getShelterId())) {
                    closeDrawer();
                    refreshData();
                } else {
                    errorLabel.setText("Gagal menghapus shelter.");
                }
            }
        });
    }

    @Override
    public void refreshData() {
        renderCards(searchField.getText());
    }
}
