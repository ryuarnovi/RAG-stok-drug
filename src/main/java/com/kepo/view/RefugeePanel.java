package com.kepo.view;

import com.kepo.model.Refugee;
import com.kepo.model.Shelter;
import com.kepo.service.RefugeeService;
import com.kepo.service.ShelterService;
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

public class RefugeePanel extends VBox implements RefreshablePanel {

    private final RefugeeService refugeeService;
    private final ShelterService shelterService;
    private final MainLayout mainLayout;

    private FlowPane cardsGrid;
    private TextField searchField;

    private TextField nameField;
    private TextField nikField;
    private TextField ageField;
    private ComboBox<String> genderCombo;
    private ComboBox<Shelter> shelterCombo;
    private ComboBox<String> statusCombo;
    private TextArea medicalNotesArea;
    private Label errorLabel;

    private Button checkInBtn;
    private Button checkOutBtn;
    private Button deleteBtn;

    // Side Drawer Simulation
    private VBox drawer;
    private Label drawerTitle;

    private Refugee selectedRefugee;

    public RefugeePanel(RefugeeService refugeeService, ShelterService shelterService, MainLayout mainLayout) {
        this.refugeeService = refugeeService;
        this.shelterService = shelterService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        // --- Header Row ---
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Manajemen Data Pengungsi");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Cari nama atau NIK...");
        searchField.setPrefWidth(220);
        searchField.setStyle(ThemeConstants.INPUT_STYLE);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards(newVal));

        Button newRefugeeBtn = new Button("Registrasi Pengungsi");
        newRefugeeBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newRefugeeBtn.setOnAction(e -> openDrawerForCreate());

        header.getChildren().addAll(title, spacer, searchField, newRefugeeBtn);
        getChildren().add(header);

        // --- Main Split Content (Cards list + Drawer) ---
        HBox mainBody = new HBox(20);
        VBox.setVgrow(mainBody, Priority.ALWAYS);

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
        drawer = new VBox(12);
        drawer.setPrefWidth(350);
        drawer.setPadding(new Insets(20));
        drawer.setStyle(ThemeConstants.CARD_STYLE);

        ScrollPane formScroll = new ScrollPane();
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox scrollContent = new VBox(10);
        scrollContent.setStyle("-fx-background-color: transparent;");

        drawerTitle = new Label("Registrasi / Check-In");
        drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        drawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        nameField = new TextField();
        nameField.setPromptText("Nama lengkap pengungsi");
        nameField.setStyle(ThemeConstants.INPUT_STYLE);

        nikField = new TextField();
        nikField.setPromptText("16 digit Nomor Induk Kependudukan");
        nikField.setStyle(ThemeConstants.INPUT_STYLE);

        ageField = new TextField();
        ageField.setPromptText("Usia pengungsi");
        ageField.setStyle(ThemeConstants.INPUT_STYLE);

        genderCombo = new ComboBox<>(FXCollections.observableArrayList("Laki-laki", "Perempuan"));
        genderCombo.setValue("Laki-laki");
        genderCombo.setMaxWidth(Double.MAX_VALUE);
        genderCombo.setStyle(ThemeConstants.INPUT_STYLE);

        shelterCombo = new ComboBox<>();
        shelterCombo.setMaxWidth(Double.MAX_VALUE);
        shelterCombo.setStyle(ThemeConstants.INPUT_STYLE);

        statusCombo = new ComboBox<>(FXCollections.observableArrayList("CHECKED_IN", "CHECKED_OUT"));
        statusCombo.setValue("CHECKED_IN");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle(ThemeConstants.INPUT_STYLE);

        medicalNotesArea = new TextArea();
        medicalNotesArea.setPromptText("Catatan keluhan medis pengungsi (ISPA, luka bakar, riwayat penyakit)...");
        medicalNotesArea.setPrefHeight(70);
        medicalNotesArea.setWrapText(true);
        medicalNotesArea.setStyle(ThemeConstants.INPUT_STYLE);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

        HBox btnRow1 = new HBox(8);
        Button saveBtn = new Button("Simpan");
        saveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        saveBtn.setOnAction(e -> handleSave());

        Button resetBtn = new Button("Tutup");
        resetBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        resetBtn.setOnAction(e -> closeDrawer());

        deleteBtn = new Button("Hapus");
        deleteBtn.setStyle(ThemeConstants.DANGER_BTN_STYLE);
        deleteBtn.setOnAction(e -> handleDelete());
        btnRow1.getChildren().addAll(saveBtn, resetBtn, deleteBtn);

        HBox btnRow2 = new HBox(8);
        checkInBtn = new Button("Check-In");
        checkInBtn.setStyle(ThemeConstants.SECONDARY_BTN_STYLE);
        checkInBtn.setOnAction(e -> handleCheckInStatus());

        checkOutBtn = new Button("Check-Out");
        checkOutBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        checkOutBtn.setOnAction(e -> handleCheckOutStatus());
        btnRow2.getChildren().addAll(checkInBtn, checkOutBtn);

        scrollContent.getChildren().addAll(
                drawerTitle,
                createFormLabel("Nama Pengungsi"), nameField,
                createFormLabel("NIK"), nikField,
                createFormLabel("Usia"), ageField,
                createFormLabel("Gender"), genderCombo,
                createFormLabel("Pilih Shelter"), shelterCombo,
                createFormLabel("Status"), statusCombo,
                createFormLabel("Catatan Medis"), medicalNotesArea,
                errorLabel,
                btnRow1,
                new Separator(),
                btnRow2
        );

        formScroll.setContent(scrollContent);
        drawer.getChildren().add(formScroll);

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
        List<Refugee> list = refugeeService.getAllRefugees();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            list = list.stream()
                    .filter(r -> r.getName().toLowerCase().contains(q) ||
                            r.getNik().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        for (Refugee r : list) {
            VBox card = new VBox(10);
            card.setPrefWidth(240);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // Name
            Label nameLabel = new Label(r.getName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 15px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(208);
            nameLabel.setPrefWidth(208);

            // NIK
            Label nikLabel = new Label("NIK: " + r.getNik());
            nikLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");

            // Gender & Age
            Label infoLabel = new Label(r.getGender() + ", " + r.getAge() + " tahun");
            infoLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");

            // Posko / Shelter
            Label shelterLabel = new Label("Posko: " + (r.getShelterName() != null ? r.getShelterName() : "-"));
            shelterLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            shelterLabel.setWrapText(true);
            shelterLabel.setMaxWidth(208);
            shelterLabel.setPrefWidth(208);

            // Notes
            String notesStr = r.getMedicalNotes() != null && !r.getMedicalNotes().isBlank() ? r.getMedicalNotes() : "Tidak ada keluhan medis.";
            Label notesLabel = new Label("Medis: " + notesStr);
            notesLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            notesLabel.setWrapText(true);
            notesLabel.setMaxWidth(208);
            notesLabel.setPrefWidth(208);

            // Status Badge
            Label badge = new Label();
            if ("CHECKED_IN".equals(r.getStatus())) {
                badge.setText("CHECKED IN");
                badge.setStyle(ThemeConstants.BADGE_SAFE);
            } else {
                badge.setText("CHECKED OUT");
                badge.setStyle(ThemeConstants.BADGE_CLOSED);
            }

            HBox badgeRow = new HBox(badge);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            // Details action button
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + " -fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(evt -> openDrawerForEdit(r));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, nikLabel, infoLabel, shelterLabel, notesLabel, badgeRow, footer);
            cardsGrid.getChildren().add(card);
        }
    }

    private void openDrawerForCreate() {
        this.selectedRefugee = null;
        drawerTitle.setText("Registrasi Pengungsi Baru");

        nameField.clear();
        nikField.clear();
        ageField.clear();
        genderCombo.setValue("Laki-laki");
        shelterCombo.setValue(null);
        statusCombo.setValue("CHECKED_IN");
        medicalNotesArea.clear();

        checkInBtn.setDisable(true);
        checkOutBtn.setDisable(true);
        deleteBtn.setVisible(false);
        errorLabel.setText("");

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void openDrawerForEdit(Refugee r) {
        this.selectedRefugee = r;
        drawerTitle.setText("Edit Data Pengungsi");

        nameField.setText(r.getName());
        nikField.setText(r.getNik());
        ageField.setText(String.valueOf(r.getAge()));
        genderCombo.setValue(r.getGender());
        statusCombo.setValue(r.getStatus());
        medicalNotesArea.setText(r.getMedicalNotes());

        // Set shelter combo
        if (r.getShelterId() != null) {
            for (Shelter s : shelterCombo.getItems()) {
                if (s.getShelterId() == r.getShelterId()) {
                    shelterCombo.setValue(s);
                    break;
                }
            }
        } else {
            shelterCombo.setValue(null);
        }

        checkInBtn.setDisable("CHECKED_IN".equals(r.getStatus()));
        checkOutBtn.setDisable("CHECKED_OUT".equals(r.getStatus()));
        deleteBtn.setVisible(true);
        errorLabel.setText("");

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void closeDrawer() {
        drawer.setVisible(false);
        drawer.setManaged(false);
        selectedRefugee = null;
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String nik = nikField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = genderCombo.getValue();
        Shelter shelter = shelterCombo.getValue();
        String status = statusCombo.getValue();
        String notes = medicalNotesArea.getText().trim();

        if (name.isEmpty() || nik.isEmpty() || ageStr.isEmpty() || shelter == null) {
            errorLabel.setText("Nama, NIK, Usia, dan Shelter harus diisi.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Usia harus berupa angka.");
            return;
        }

        Refugee r = selectedRefugee;
        if (r == null) {
            r = new Refugee();
        }
        r.setName(name);
        r.setNik(nik);
        r.setAge(age);
        r.setGender(gender);
        r.setShelterId(shelter.getShelterId());
        r.setStatus(status);
        r.setMedicalNotes(notes);

        if (refugeeService.saveRefugee(r)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyimpan data pengungsi.");
        }
    }

    private void handleCheckInStatus() {
        if (selectedRefugee == null) {
            errorLabel.setText("Pilih pengungsi terlebih dahulu.");
            return;
        }
        Shelter s = shelterCombo.getValue();
        if (s == null) {
            errorLabel.setText("Pilih shelter tujuan check-in.");
            return;
        }
        if (refugeeService.checkIn(selectedRefugee, s.getShelterId())) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal memproses Check-In.");
        }
    }

    private void handleCheckOutStatus() {
        if (selectedRefugee == null) {
            errorLabel.setText("Pilih pengungsi terlebih dahulu.");
            return;
        }
        if (refugeeService.checkOut(selectedRefugee)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal memproses Check-Out.");
        }
    }

    private void handleDelete() {
        if (selectedRefugee == null) {
            errorLabel.setText("Pilih pengungsi yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus data pengungsi " + selectedRefugee.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (refugeeService.deleteRefugee(selectedRefugee.getRefugeeId())) {
                    closeDrawer();
                    refreshData();
                } else {
                    errorLabel.setText("Gagal menghapus pengungsi.");
                }
            }
        });
    }

    @Override
    public void refreshData() {
        // Refresh Shelters dropdown
        List<Shelter> shelters = shelterService.getAllShelters();
        shelterCombo.setItems(FXCollections.observableArrayList(shelters));

        // Refresh Cards list
        renderCards(searchField != null ? searchField.getText() : null);
    }
}
