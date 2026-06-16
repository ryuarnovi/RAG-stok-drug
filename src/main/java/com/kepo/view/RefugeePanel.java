package com.kepo.view;

import com.kepo.model.Refugee;
import com.kepo.model.Shelter;
import com.kepo.controller.RefugeeShelterController;
import com.kepo.service.RefugeeService;
import com.kepo.service.ShelterService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private final RefugeeShelterController refugeeShelterController;
    private final MainLayout mainLayout;

    private FlowPane cardsGrid;
    private TextField searchField;

    private TextField nameField;
    private TextField nikField;
    private TextField ageField;
    private ComboBox<String> genderCombo;
    private ComboBox<Shelter> shelterCombo;
    private ComboBox<String> statusCombo;
    private ComboBox<String> priorityCombo;
    private TextField familyCodeField;
    private TextArea medicalNotesArea;
    private Label errorLabel;

    private Button checkInBtn;
    private Button checkOutBtn;
    private Button deleteBtn;

    // Transfer & History sub-components
    private VBox transferSection;
    private ComboBox<Shelter> transferShelterCombo;
    private TextArea transferNotesArea;
    private VBox historySection;

    // Side Drawer Simulation
    private VBox drawer;
    private Label drawerTitle;

    private Refugee selectedRefugee;

    public RefugeePanel(RefugeeService refugeeService, ShelterService shelterService, RefugeeShelterController refugeeShelterController, MainLayout mainLayout) {
        this.refugeeService = refugeeService;
        this.shelterService = shelterService;
        this.refugeeShelterController = refugeeShelterController;
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
        drawer.setPrefWidth(380);
        drawer.setPadding(new Insets(20));
        drawer.setStyle(ThemeConstants.CARD_STYLE);

        ScrollPane formScroll = new ScrollPane();
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox scrollContent = new VBox(10);
        scrollContent.setStyle("-fx-background-color: transparent;");

        drawerTitle = new Label("Registrasi / Check-In");
        drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 16));
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

        priorityCombo = new ComboBox<>(FXCollections.observableArrayList("REGULAR", "BALITA", "LANSIA", "IBU_HAMIL", "DISABILITAS", "SICK"));
        priorityCombo.setValue("REGULAR");
        priorityCombo.setMaxWidth(Double.MAX_VALUE);
        priorityCombo.setStyle(ThemeConstants.INPUT_STYLE);

        familyCodeField = new TextField();
        familyCodeField.setPromptText("Kode Keluarga (opsional, misal: FAM-001)");
        familyCodeField.setStyle(ThemeConstants.INPUT_STYLE);

        medicalNotesArea = new TextArea();
        medicalNotesArea.setPromptText("Catatan keluhan medis pengungsi (ISPA, luka bakar, riwayat penyakit)...");
        medicalNotesArea.setPrefHeight(60);
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

        // --- Transfer Section ---
        transferSection = new VBox(8);
        transferSection.setPadding(new Insets(10, 0, 0, 0));
        Label transferTitle = new Label("Pindahkan ke Shelter Lain");
        transferTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 13));
        transferTitle.setTextFill(Color.web(ThemeConstants.PRIMARY));

        transferShelterCombo = new ComboBox<>();
        transferShelterCombo.setMaxWidth(Double.MAX_VALUE);
        transferShelterCombo.setStyle(ThemeConstants.INPUT_STYLE);

        transferNotesArea = new TextArea();
        transferNotesArea.setPromptText("Alasan pemindahan...");
        transferNotesArea.setPrefHeight(50);
        transferNotesArea.setWrapText(true);
        transferNotesArea.setStyle(ThemeConstants.INPUT_STYLE);

        Button executeTransferBtn = new Button("Proses Pemindahan");
        executeTransferBtn.setStyle(ThemeConstants.SECONDARY_BTN_STYLE);
        executeTransferBtn.setMaxWidth(Double.MAX_VALUE);
        executeTransferBtn.setOnAction(e -> handleTransfer());

        transferSection.getChildren().addAll(transferTitle, transferShelterCombo, transferNotesArea, executeTransferBtn);

        // --- History Section ---
        historySection = new VBox(6);
        historySection.setPadding(new Insets(10, 0, 0, 0));
        Label historyTitle = new Label("Riwayat Perpindahan");
        historyTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 13));
        historyTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        historySection.getChildren().addAll(historyTitle);

        scrollContent.getChildren().addAll(
                drawerTitle,
                createFormLabel("Nama Pengungsi"), nameField,
                createFormLabel("NIK"), nikField,
                createFormLabel("Usia"), ageField,
                createFormLabel("Gender"), genderCombo,
                createFormLabel("Kelompok Prioritas"), priorityCombo,
                createFormLabel("Kode Keluarga"), familyCodeField,
                createFormLabel("Pilih Shelter"), shelterCombo,
                createFormLabel("Status"), statusCombo,
                createFormLabel("Catatan Medis"), medicalNotesArea,
                errorLabel,
                btnRow1,
                new Separator(),
                btnRow2,
                new Separator(),
                transferSection,
                new Separator(),
                historySection
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
        label.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-weight: bold; -fx-font-size: 11px;");
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
            VBox card = new VBox(8);
            card.setPrefWidth(250);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // Name
            Label nameLabel = new Label(r.getName());
            nameLabel.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
            nameLabel.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(218);

            // Gender & Age
            Label infoLabel = new Label(r.getGender() + ", " + r.getAge() + " tahun");
            infoLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");

            // NIK & Family Code
            String famStr = (r.getFamilyCode() != null && !r.getFamilyCode().isBlank()) ? " | Kel: " + r.getFamilyCode() : "";
            Label nikLabel = new Label("NIK: " + r.getNik() + famStr);
            nikLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");

            // Posko / Shelter
            Label shelterLabel = new Label("Posko: " + (r.getShelterName() != null ? r.getShelterName() : "-"));
            shelterLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            shelterLabel.setWrapText(true);

            // Notes
            String notesStr = r.getMedicalNotes() != null && !r.getMedicalNotes().isBlank() ? r.getMedicalNotes() : "Tidak ada keluhan medis.";
            Label notesLabel = new Label("Medis: " + notesStr);
            notesLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            notesLabel.setWrapText(true);

            // Badges Row
            HBox badgeRow = new HBox(6);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            // Status Badge
            Label statusBadge = new Label();
            if ("CHECKED_IN".equals(r.getStatus())) {
                statusBadge.setText("CHECKED IN");
                statusBadge.setStyle(ThemeConstants.BADGE_SAFE);
            } else {
                statusBadge.setText("CHECKED OUT");
                statusBadge.setStyle(ThemeConstants.BADGE_CLOSED);
            }
            badgeRow.getChildren().add(statusBadge);

            // Priority Badge
            if (r.getPriorityStatus() != null && !r.getPriorityStatus().equals("REGULAR")) {
                Label prioBadge = new Label(r.getPriorityStatus());
                if ("SICK".equals(r.getPriorityStatus()) || "DISABILITAS".equals(r.getPriorityStatus())) {
                    prioBadge.setStyle(ThemeConstants.BADGE_CRITICAL);
                } else if ("LANSIA".equals(r.getPriorityStatus()) || "IBU_HAMIL".equals(r.getPriorityStatus())) {
                    prioBadge.setStyle(ThemeConstants.BADGE_WARNING);
                } else {
                    prioBadge.setStyle(ThemeConstants.BADGE_ACTIVE);
                }
                badgeRow.getChildren().add(prioBadge);
            }

            // Details action button
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + " -fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(evt -> openDrawerForEdit(r));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, infoLabel, nikLabel, shelterLabel, notesLabel, badgeRow, footer);
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
        priorityCombo.setValue("REGULAR");
        familyCodeField.clear();
        medicalNotesArea.clear();

        checkInBtn.setDisable(true);
        checkOutBtn.setDisable(true);
        deleteBtn.setVisible(false);
        errorLabel.setText("");

        transferSection.setVisible(false);
        transferSection.setManaged(false);
        historySection.setVisible(false);
        historySection.setManaged(false);

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
        priorityCombo.setValue(r.getPriorityStatus() != null ? r.getPriorityStatus() : "REGULAR");
        familyCodeField.setText(r.getFamilyCode() != null ? r.getFamilyCode() : "");
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

        // Setup transfer controls
        boolean canTransfer = "CHECKED_IN".equals(r.getStatus());
        transferSection.setVisible(canTransfer);
        transferSection.setManaged(canTransfer);
        transferNotesArea.clear();
        if (canTransfer) {
            List<Shelter> shelters = shelterService.getAllShelters();
            transferShelterCombo.setItems(FXCollections.observableArrayList(shelters));
            if (r.getShelterId() != null) {
                for (Shelter s : transferShelterCombo.getItems()) {
                    if (s.getShelterId() == r.getShelterId()) {
                        transferShelterCombo.setValue(s);
                        break;
                    }
                }
            }
        }

        // Load history logs
        historySection.setVisible(true);
        historySection.setManaged(true);
        refreshHistoryList(r.getRefugeeId());

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void refreshHistoryList(int refugeeId) {
        // Clear all except the title label
        Node titleLabel = historySection.getChildren().get(0);
        historySection.getChildren().clear();
        historySection.getChildren().add(titleLabel);

        List<com.kepo.model.RefugeeMovement> history = refugeeShelterController.getMovementHistory(refugeeId);
        if (history.isEmpty()) {
            Label emptyLbl = new Label("Belum ada riwayat pemindahan.");
            emptyLbl.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-style: italic; -fx-font-size: 11px;");
            historySection.getChildren().add(emptyLbl);
        } else {
            for (com.kepo.model.RefugeeMovement m : history) {
                String from = m.getFromShelterName() != null ? m.getFromShelterName() : "Registrasi Awal";
                String to = m.getToShelterName() != null ? m.getToShelterName() : "Check-Out";
                String text = String.format("- %s -> %s\n  Oleh: %s pada %s\n  Catatan: %s", 
                        from, to, m.getMovedBy(), m.getMovedAt().toString().substring(0, 16), 
                        (m.getNotes() != null && !m.getNotes().isBlank() ? m.getNotes() : "-"));
                
                Label logLabel = new Label(text);
                logLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
                logLabel.setWrapText(true);
                historySection.getChildren().add(logLabel);
            }
        }
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
        String priority = priorityCombo.getValue();
        String familyCode = familyCodeField.getText().trim();
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
        r.setPriorityStatus(priority);
        r.setFamilyCode(familyCode);
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

    private void handleTransfer() {
        if (selectedRefugee == null) {
            errorLabel.setText("Pilih pengungsi terlebih dahulu.");
            return;
        }
        Shelter target = transferShelterCombo.getValue();
        if (target == null) {
            errorLabel.setText("Pilih shelter tujuan transfer.");
            return;
        }
        String notes = transferNotesArea.getText().trim();
        if (refugeeShelterController.transferRefugee(selectedRefugee.getRefugeeId(), target.getShelterId(), notes)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal memindahkan shelter pengungsi.");
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
