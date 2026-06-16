package com.kepo.view;

import com.kepo.model.Distribution;
import com.kepo.model.Shelter;
import com.kepo.model.Medicine;
import com.kepo.controller.InventoryController;
import com.kepo.controller.RefugeeShelterController;
import com.kepo.service.DistributionService;
import com.kepo.service.ShelterService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DistributionPanel extends VBox implements RefreshablePanel {

    private final DistributionService distributionService;
    private final ShelterService shelterService;
    private final InventoryController inventoryController;
    private final RefugeeShelterController refugeeShelterController;
    private final MainLayout mainLayout;

    private FlowPane cardsGrid;
    private TextField searchField;

    private TextField docNumField;
    private ComboBox<Shelter> shelterCombo;
    private ComboBox<Medicine> itemCombo;
    private TextField qtyField;
    private TextArea notesArea;
    private ComboBox<String> statusCombo;
    private Label errorLabel;

    private Button saveBtn;
    private Button addMedBtn;
    private VBox allocationsContainer;
    private List<MedicineAllocation> currentAllocations = new ArrayList<>();

    private Button approveBtn;
    private Button shipBtn;
    private Button receiveBtn;
    private Button deleteBtn;

    // Side Drawer Simulation
    private VBox drawer;
    private Label drawerTitle;

    // Shelter stocks displays
    private VBox shelterStocksSection;
    private VBox shelterStocksContainer;

    private Distribution selectedDistribution;

    public DistributionPanel(DistributionService distributionService, ShelterService shelterService, InventoryController inventoryController, RefugeeShelterController refugeeShelterController, MainLayout mainLayout) {
        this.distributionService = distributionService;
        this.shelterService = shelterService;
        this.inventoryController = inventoryController;
        this.refugeeShelterController = refugeeShelterController;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        // --- Header Row ---
        Label title = new Label("Manajemen Distribusi Logistik Bantuan");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        getChildren().add(title);

        // --- Search & Create Button Section ---
        HBox searchRow = new HBox(15);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Cari no dokumen atau shelter...");
        searchField.setStyle(ThemeConstants.INPUT_STYLE);
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newDocBtn = new Button("Tambah Alokasi Logistik");
        newDocBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newDocBtn.setOnAction(e -> openDrawerForCreate());

        searchRow.getChildren().addAll(searchField, spacer, newDocBtn);
        getChildren().add(searchRow);

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

        drawerTitle = new Label("Dokumen Alokasi Bantuan");
        drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        drawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        docNumField = new TextField();
        docNumField.setPromptText("DIST-YYYYMMDD-000");
        docNumField.setStyle(ThemeConstants.INPUT_STYLE);

        shelterCombo = new ComboBox<>();
        shelterCombo.setMaxWidth(Double.MAX_VALUE);
        shelterCombo.setStyle(ThemeConstants.INPUT_STYLE);
        shelterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateShelterStocksDisplay(newVal);
        });

        itemCombo = new ComboBox<>();
        itemCombo.setMaxWidth(Double.MAX_VALUE);
        itemCombo.setStyle(ThemeConstants.INPUT_STYLE);
        itemCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medicine item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getMedicineName() + " (" + item.getMedicineCode() + ") - Stok: " + item.getStockQuantity() + " " + item.getUnit());
                    setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + ";");
                }
            }
        });
        itemCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Medicine item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getMedicineName() + " (" + item.getMedicineCode() + ")");
                    setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + ";");
                }
            }
        });

        qtyField = new TextField();
        qtyField.setPromptText("Kuantitas bantuan");
        qtyField.setStyle(ThemeConstants.INPUT_STYLE);

        addMedBtn = new Button("Tambah Obat");
        addMedBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        addMedBtn.setMaxWidth(Double.MAX_VALUE);
        addMedBtn.setOnAction(e -> handleAddAllocation());

        allocationsContainer = new VBox(8);
        allocationsContainer.setStyle("-fx-background-color: transparent;");

        statusCombo = new ComboBox<>(FXCollections.observableArrayList("DRAFT", "APPROVED", "SHIPPED", "RECEIVED"));
        statusCombo.setValue("DRAFT");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle(ThemeConstants.INPUT_STYLE);
        statusCombo.setDisable(true);

        notesArea = new TextArea();
        notesArea.setPromptText("Catatan pengiriman (misal: isi karton, penerima lapangan)...");
        notesArea.setPrefHeight(60);
        notesArea.setWrapText(true);
        notesArea.setStyle(ThemeConstants.INPUT_STYLE);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

        HBox btnRow1 = new HBox(8);
        saveBtn = new Button("Simpan");
        saveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        saveBtn.setOnAction(e -> handleSave());

        Button resetBtn = new Button("Tutup");
        resetBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        resetBtn.setOnAction(e -> closeDrawer());

        deleteBtn = new Button("Hapus");
        deleteBtn.setStyle(ThemeConstants.DANGER_BTN_STYLE);
        deleteBtn.setOnAction(e -> handleDelete());
        btnRow1.getChildren().addAll(saveBtn, resetBtn, deleteBtn);

        // Workflow Action Row
        VBox workflowBox = new VBox(8);
        Label workflowLabel = new Label("Alur Persetujuan (Workflow):");
        workflowLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        approveBtn = new Button("Setujui (Approve)");
        approveBtn.setMaxWidth(Double.MAX_VALUE);
        approveBtn.setStyle(ThemeConstants.SECONDARY_BTN_STYLE);
        approveBtn.setOnAction(e -> handleApprove());

        shipBtn = new Button("Kirim (Ship)");
        shipBtn.setMaxWidth(Double.MAX_VALUE);
        shipBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        shipBtn.setOnAction(e -> handleShip());

        receiveBtn = new Button("Terima (Receive)");
        receiveBtn.setMaxWidth(Double.MAX_VALUE);
        receiveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE + " -fx-background-color: #3b82f6;");
        receiveBtn.setOnAction(e -> handleReceive());

        workflowBox.getChildren().addAll(workflowLabel, approveBtn, shipBtn, receiveBtn);

        // Shelter Stocks Display Panel
        shelterStocksSection = new VBox(6);
        Label sStockTitle = new Label("Stok & Kesiapan Logistik Posko:");
        sStockTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 12));
        sStockTitle.setTextFill(Color.web(ThemeConstants.PRIMARY));
        shelterStocksContainer = new VBox(4);
        shelterStocksSection.getChildren().addAll(sStockTitle, shelterStocksContainer);
        shelterStocksSection.setVisible(false);
        shelterStocksSection.setManaged(false);

        scrollContent.getChildren().addAll(
                drawerTitle,
                createFormLabel("No Dokumen"), docNumField,
                createFormLabel("Shelter Tujuan"), shelterCombo,
                shelterStocksSection,
                new Separator(),
                createFormLabel("Pilih Obat"), itemCombo,
                createFormLabel("Jumlah Qty"), qtyField,
                addMedBtn,
                createFormLabel("Daftar Alokasi Obat"), allocationsContainer,
                createFormLabel("Status Dokumen"), statusCombo,
                createFormLabel("Keterangan"), notesArea,
                errorLabel,
                btnRow1,
                new Separator(),
                workflowBox
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
        List<Distribution> list = distributionService.getAllDistributions();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            list = list.stream()
                    .filter(d -> d.getDocNum().toLowerCase().contains(q) ||
                            d.getShelterName().toLowerCase().contains(q) ||
                            d.getItemType().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        for (Distribution d : list) {
            VBox card = new VBox(10);
            card.setPrefWidth(260);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // Document Number
            Label docLabel = new Label(d.getDocNum());
            docLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            // Shelter Name
            Label shelterLabel = new Label("Ke: " + d.getShelterName());
            shelterLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 12px;");
            shelterLabel.setWrapText(true);
            shelterLabel.setMaxWidth(228);
            shelterLabel.setPrefWidth(228);

            // Item Type & Qty
            Label itemLabel = new Label("Obat: " + d.getItemType() + " (" + d.getQuantity() + " pcs)");
            itemLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");
            itemLabel.setWrapText(true);
            itemLabel.setMaxWidth(228);
            itemLabel.setPrefWidth(228);

            // Notes Snippet
            String notesStr = d.getNotes() != null && !d.getNotes().isBlank() ? d.getNotes() : "Tidak ada catatan.";
            Label notesLabel = new Label("Info: " + notesStr);
            notesLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            notesLabel.setWrapText(true);
            notesLabel.setMaxWidth(228);
            notesLabel.setPrefWidth(228);

            // Progress status timeline HBox
            HBox timeline = createTimeline(d.getStatus());

            // Details action button
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + " -fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(evt -> openDrawerForEdit(d));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(docLabel, shelterLabel, itemLabel, notesLabel, new Separator(), timeline, footer);
            cardsGrid.getChildren().add(card);
        }
    }

    private HBox createTimeline(String currentStatus) {
        HBox timeline = new HBox(4);
        timeline.setAlignment(Pos.CENTER_LEFT);

        String[] stages = {"DRAFT", "APPROVED", "SHIPPED", "RECEIVED"};
        for (int i = 0; i < stages.length; i++) {
            String stage = stages[i];
            Label label = new Label(stage);
            label.setFont(Font.font("Inter", FontWeight.BOLD, 9));

            if (stage.equals(currentStatus)) {
                String activeStyle = switch (stage) {
                    case "DRAFT" -> "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 4; -fx-padding: 2 4 2 4;";
                    case "APPROVED" -> "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-background-radius: 4; -fx-padding: 2 4 2 4;";
                    case "SHIPPED" -> "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-background-radius: 4; -fx-padding: 2 4 2 4;";
                    case "RECEIVED" -> "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4; -fx-padding: 2 4 2 4;";
                    default -> "";
                };
                label.setStyle(activeStyle);
            } else {
                label.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 2 4 2 4;");
            }
            timeline.getChildren().add(label);

            if (i < stages.length - 1) {
                Label arrow = new Label("→");
                arrow.setFont(Font.font("Inter", FontWeight.BOLD, 9));
                arrow.setTextFill(Color.web("#cbd5e1"));
                timeline.getChildren().add(arrow);
            }
        }
        return timeline;
    }

    private void openDrawerForCreate() {
        this.selectedDistribution = null;
        drawerTitle.setText("Buat Dokumen Distribusi");
        
        // Generate auto doc num
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        docNumField.setText("DIST-" + timestamp);
        docNumField.setDisable(false);

        itemCombo.setValue(null);
        qtyField.clear();
        statusCombo.setValue("DRAFT");
        notesArea.clear();
        shelterCombo.setValue(null);

        currentAllocations.clear();
        renderAllocations();

        // Enable editing controls for DRAFT
        shelterCombo.setDisable(false);
        itemCombo.setDisable(false);
        qtyField.setDisable(false);
        addMedBtn.setDisable(false);
        notesArea.setDisable(false);
        saveBtn.setDisable(false);

        approveBtn.setDisable(true);
        shipBtn.setDisable(true);
        receiveBtn.setDisable(true);
        deleteBtn.setVisible(false);
        errorLabel.setText("");

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void openDrawerForEdit(Distribution d) {
        this.selectedDistribution = d;
        drawerTitle.setText("Detail Dokumen Distribusi");

        docNumField.setText(d.getDocNum());
        docNumField.setDisable(true);
        
        // Clear combo/qty fields to start fresh
        itemCombo.setValue(null);
        qtyField.clear();
        statusCombo.setValue(d.getStatus());

        // Parse allocations from notes or legacy fallback
        currentAllocations.clear();
        String notesText = d.getNotes();
        if (notesText != null && notesText.contains("[ALLOCATION_DATA:")) {
            int start = notesText.indexOf("[ALLOCATION_DATA:") + "[ALLOCATION_DATA:".length();
            int end = notesText.indexOf("]", start);
            if (end > start) {
                String data = notesText.substring(start, end);
                String[] items = data.split(";");
                for (String item : items) {
                    if (item.contains(":")) {
                        String[] parts = item.split(":");
                        if (parts.length == 2) {
                            String code = parts[0];
                            int qty = Integer.parseInt(parts[1]);
                            // Look up medicine to resolve name/unit
                            Medicine med = inventoryController.getMedicineByCode(code);
                            String name = med != null ? med.getMedicineName() : code;
                            String unit = med != null ? med.getUnit() : "pcs";
                            currentAllocations.add(new MedicineAllocation(code, name, qty, unit));
                        }
                    }
                }
            }
        } else {
            // Legacy fallback
            String currentItemType = d.getItemType();
            String targetCode = null;
            if (currentItemType != null && currentItemType.contains("(") && currentItemType.endsWith(")")) {
                targetCode = currentItemType.substring(currentItemType.lastIndexOf("(") + 1, currentItemType.length() - 1);
            } else {
                targetCode = currentItemType;
            }
            if (targetCode != null && !targetCode.isBlank()) {
                Medicine med = inventoryController.getMedicineByCode(targetCode);
                String name = med != null ? med.getMedicineName() : targetCode;
                String unit = med != null ? med.getUnit() : "pcs";
                currentAllocations.add(new MedicineAllocation(targetCode, name, d.getQuantity(), unit));
            }
        }
        renderAllocations();

        // Render clean editable notes, hiding allocations tag
        if (notesText != null) {
            String cleanNotes = notesText;
            if (cleanNotes.contains("\n\n--- Alokasi Obat ---")) {
                cleanNotes = cleanNotes.substring(0, cleanNotes.indexOf("\n\n--- Alokasi Obat ---"));
            } else if (cleanNotes.contains("[ALLOCATION_DATA:")) {
                cleanNotes = cleanNotes.substring(0, cleanNotes.indexOf("[ALLOCATION_DATA:"));
            }
            notesArea.setText(cleanNotes.trim());
        } else {
            notesArea.clear();
        }

        // Set shelter combo
        for (Shelter s : shelterCombo.getItems()) {
            if (s.getShelterId() == d.getShelterId()) {
                shelterCombo.setValue(s);
                break;
            }
        }

        // Lock form inputs if document is not in DRAFT status
        boolean isDraft = "DRAFT".equals(d.getStatus());
        shelterCombo.setDisable(!isDraft);
        itemCombo.setDisable(!isDraft);
        qtyField.setDisable(!isDraft);
        addMedBtn.setDisable(!isDraft);
        notesArea.setDisable(false); // Notes can still be edited
        saveBtn.setDisable(false);

        // Enable/disable workflow buttons based on status
        approveBtn.setDisable(!"DRAFT".equals(d.getStatus()));
        shipBtn.setDisable(!"APPROVED".equals(d.getStatus()));
        receiveBtn.setDisable(!"SHIPPED".equals(d.getStatus()));
        deleteBtn.setVisible(isDraft); // Only drafts can be deleted
        errorLabel.setText("");

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void closeDrawer() {
        drawer.setVisible(false);
        drawer.setManaged(false);
        selectedDistribution = null;
    }

    private void handleSave() {
        String docNum = docNumField.getText().trim();
        Shelter shelter = shelterCombo.getValue();
        String status = statusCombo.getValue();
        String notes = notesArea.getText().trim();

        if (docNum.isEmpty() || shelter == null) {
            errorLabel.setText("No Dokumen dan Shelter harus diisi.");
            return;
        }

        if (currentAllocations.isEmpty()) {
            errorLabel.setText("Pilih minimal satu obat untuk dialokasikan.");
            return;
        }

        // Validate quantities for allocations
        int totalQty = 0;
        for (MedicineAllocation alloc : currentAllocations) {
            if (alloc.getQuantity() <= 0) {
                errorLabel.setText("Kuantitas untuk obat " + alloc.getMedicineName() + " harus lebih dari 0.");
                return;
            }
            totalQty += alloc.getQuantity();
        }

        Distribution d = selectedDistribution;
        if (d == null) {
            d = new Distribution();
        }
        d.setDocNum(docNum);
        d.setShelterId(shelter.getShelterId());
        
        // Generate item type based on allocations
        String itemTypeVal = "";
        if (currentAllocations.size() == 1) {
            MedicineAllocation first = currentAllocations.get(0);
            itemTypeVal = first.getMedicineName() + " (" + first.getMedicineCode() + ")";
            if (itemTypeVal.length() > 50) {
                itemTypeVal = first.getMedicineCode();
            }
        } else {
            StringBuilder codes = new StringBuilder();
            for (int i = 0; i < currentAllocations.size(); i++) {
                if (i > 0) codes.append(", ");
                codes.append(currentAllocations.get(i).getMedicineCode());
            }
            itemTypeVal = codes.toString();
            if (itemTypeVal.length() > 50) {
                itemTypeVal = "Multi-Obat (" + currentAllocations.size() + " jenis)";
            }
        }
        d.setItemType(itemTypeVal);
        d.setQuantity(totalQty);
        d.setStatus(status);

        // Serialize allocations block to notes
        StringBuilder fullNotes = new StringBuilder(notes);
        fullNotes.append("\n\n--- Alokasi Obat ---");
        for (MedicineAllocation alloc : currentAllocations) {
            fullNotes.append("\n- ").append(alloc.getMedicineName()).append(" (").append(alloc.getMedicineCode()).append("): ").append(alloc.getQuantity()).append(" ").append(alloc.getUnit());
        }
        fullNotes.append("\n[ALLOCATION_DATA:");
        for (MedicineAllocation alloc : currentAllocations) {
            fullNotes.append(alloc.getMedicineCode()).append(":").append(alloc.getQuantity()).append(";");
        }
        fullNotes.append("]");
        d.setNotes(fullNotes.toString());

        if (distributionService.saveDistribution(d)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyimpan dokumen distribusi.");
        }
    }

    private void handleApprove() {
        if (selectedDistribution == null) return;
        if (distributionService.approveDistribution(selectedDistribution.getDistributionId())) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyetujui dokumen.");
        }
    }

    private void handleShip() {
        if (selectedDistribution == null) return;

        // 1. Parse allocations list from notes
        List<MedicineAllocation> allocs = new ArrayList<>();
        String notesText = selectedDistribution.getNotes();
        if (notesText != null && notesText.contains("[ALLOCATION_DATA:")) {
            int start = notesText.indexOf("[ALLOCATION_DATA:") + "[ALLOCATION_DATA:".length();
            int end = notesText.indexOf("]", start);
            if (end > start) {
                String data = notesText.substring(start, end);
                String[] items = data.split(";");
                for (String item : items) {
                    if (item.contains(":")) {
                        String[] parts = item.split(":");
                        if (parts.length == 2) {
                            String code = parts[0];
                            int qty = Integer.parseInt(parts[1]);
                            // Look up medicine to resolve name/unit
                            Medicine med = inventoryController.getMedicineByCode(code);
                            String name = med != null ? med.getMedicineName() : code;
                            String unit = med != null ? med.getUnit() : "pcs";
                            allocs.add(new MedicineAllocation(code, name, qty, unit));
                        }
                    }
                }
            }
        } else {
            // Legacy fallback
            String itemType = selectedDistribution.getItemType();
            String medCode = null;
            if (itemType != null && itemType.contains("(") && itemType.endsWith(")")) {
                medCode = itemType.substring(itemType.lastIndexOf("(") + 1, itemType.length() - 1);
            } else {
                medCode = itemType;
            }
            if (medCode != null && !medCode.isBlank()) {
                Medicine med = inventoryController.getMedicineByCode(medCode);
                String name = med != null ? med.getMedicineName() : medCode;
                String unit = med != null ? med.getUnit() : "pcs";
                allocs.add(new MedicineAllocation(medCode, name, selectedDistribution.getQuantity(), unit));
            }
        }

        if (allocs.isEmpty()) {
            errorLabel.setText("Gagal: Tidak ada alokasi obat yang valid pada dokumen ini.");
            return;
        }

        // 2. Validate stock availability for ALL items
        for (MedicineAllocation alloc : allocs) {
            Medicine activeMed = inventoryController.getMedicineByCode(alloc.getMedicineCode());
            if (activeMed == null) {
                errorLabel.setText("Gagal: Obat '" + alloc.getMedicineCode() + "' tidak ditemukan di inventaris.");
                return;
            }
            if (activeMed.getStockQuantity() < alloc.getQuantity()) {
                errorLabel.setText("Gagal: Stok tidak mencukupi untuk " + alloc.getMedicineName() + ". Stok saat ini: " + activeMed.getStockQuantity() + " " + activeMed.getUnit() + " (Diminta: " + alloc.getQuantity() + ").");
                return;
            }
        }

        // 3. Deduct stock for ALL allocations
        List<MedicineAllocation> successfulDeductions = new ArrayList<>();
        boolean deductionError = false;
        String shipNotes = "Kirim ke shelter: " + selectedDistribution.getShelterName() + " (Doc: " + selectedDistribution.getDocNum() + ")";

        for (MedicineAllocation alloc : allocs) {
            Medicine activeMed = inventoryController.getMedicineByCode(alloc.getMedicineCode());
            if (activeMed != null) {
                boolean success = inventoryController.reduceStock(activeMed.getMedicineId(), alloc.getQuantity(), shipNotes);
                if (success) {
                    successfulDeductions.add(alloc);
                } else {
                    deductionError = true;
                    break;
                }
            }
        }

        if (deductionError) {
            // Rollback successful deductions
            for (MedicineAllocation successAlloc : successfulDeductions) {
                Medicine activeMed = inventoryController.getMedicineByCode(successAlloc.getMedicineCode());
                if (activeMed != null) {
                    inventoryController.addStock(activeMed.getMedicineId(), successAlloc.getQuantity(), "Rollback: Gagal ship dokumen " + selectedDistribution.getDocNum());
                }
            }
            errorLabel.setText("Gagal mengurangi stok obat di inventaris.");
            return;
        }

        // 4. Perform status transition
        if (distributionService.shipDistribution(selectedDistribution.getDistributionId())) {
            closeDrawer();
            refreshData();
        } else {
            // Rollback all deductions if status transition fails
            for (MedicineAllocation successAlloc : allocs) {
                Medicine activeMed = inventoryController.getMedicineByCode(successAlloc.getMedicineCode());
                if (activeMed != null) {
                    inventoryController.addStock(activeMed.getMedicineId(), successAlloc.getQuantity(), "Rollback: Gagal ship dokumen " + selectedDistribution.getDocNum());
                }
            }
            errorLabel.setText("Gagal mengubah status dokumen menjadi KIRIM.");
        }
    }

    private void handleReceive() {
        if (selectedDistribution == null) return;
        if (distributionService.receiveDistribution(selectedDistribution.getDistributionId())) {
            // Add items to shelter stocks
            for (MedicineAllocation alloc : currentAllocations) {
                Medicine activeMed = inventoryController.getMedicineByCode(alloc.getMedicineCode());
                if (activeMed != null) {
                    refugeeShelterController.updateShelterStock(
                        selectedDistribution.getShelterId(),
                        activeMed.getMedicineId(),
                        alloc.getQuantity()
                    );
                }
            }
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal memproses penerimaan bantuan.");
        }
    }

    private void updateShelterStocksDisplay(Shelter shelter) {
        shelterStocksContainer.getChildren().clear();
        if (shelter == null) {
            shelterStocksSection.setVisible(false);
            shelterStocksSection.setManaged(false);
            return;
        }

        shelterStocksSection.setVisible(true);
        shelterStocksSection.setManaged(true);

        List<com.kepo.model.ShelterStock> stocks = refugeeShelterController.getShelterStocks(shelter.getShelterId());
        if (stocks.isEmpty()) {
            Label emptyLbl = new Label("Belum ada stok obat di posko ini.");
            emptyLbl.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-style: italic; -fx-font-size: 11px;");
            shelterStocksContainer.getChildren().add(emptyLbl);
        } else {
            for (com.kepo.model.ShelterStock s : stocks) {
                double pct = refugeeShelterController.calculateAvailabilityPercentage(s);
                double doc = refugeeShelterController.estimateDaysOfCoverage(s, shelter.getCurrentOccupancy());
                
                String docStr = Double.isInfinite(doc) ? "Aman (0 pengungsi)" : String.format("%.1f Hari", doc);
                String style = pct < 50 ? ThemeConstants.BADGE_CRITICAL : (pct < 80 ? ThemeConstants.BADGE_WARNING : ThemeConstants.BADGE_SAFE);
                
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(2, 0, 2, 0));
                
                Label nameLbl = new Label(s.getMedicineName() + ": " + s.getQuantity() + " " + s.getUnit() + " (DoC: " + docStr + ")");
                nameLbl.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-size: 11px;");
                HBox.setHgrow(nameLbl, Priority.ALWAYS);
                
                Label alertBadge = new Label(pct < 50 ? "KRITIS" : (pct < 80 ? "WASPADA" : "AMAN"));
                alertBadge.setStyle(style + " -fx-font-size: 9px; -fx-padding: 1 5 1 5;");
                
                row.getChildren().addAll(nameLbl, alertBadge);
                shelterStocksContainer.getChildren().add(row);
            }
        }
    }

    private void handleDelete() {
        if (selectedDistribution == null) {
            errorLabel.setText("Pilih dokumen yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus dokumen " + selectedDistribution.getDocNum() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (distributionService.deleteDistribution(selectedDistribution.getDistributionId())) {
                    closeDrawer();
                    refreshData();
                } else {
                    errorLabel.setText("Gagal menghapus dokumen.");
                }
            }
        });
    }

    @Override
    public void refreshData() {
        // Refresh shelters combo
        List<Shelter> shelters = shelterService.getAllShelters();
        shelterCombo.setItems(FXCollections.observableArrayList(shelters));

        // Refresh medicines combo
        if (inventoryController != null) {
            List<Medicine> medicines = inventoryController.getAllMedicines();
            itemCombo.setItems(FXCollections.observableArrayList(medicines));
        }

        // Refresh Cards list
        renderCards(searchField != null ? searchField.getText() : null);
    }

    private void handleAddAllocation() {
        Medicine selectedMed = itemCombo.getValue();
        String qtyStr = qtyField.getText().trim();

        if (selectedMed == null) {
            errorLabel.setText("Pilih obat terlebih dahulu.");
            return;
        }

        if (qtyStr.isEmpty()) {
            errorLabel.setText("Masukkan kuantitas obat.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Kuantitas harus berupa angka.");
            return;
        }

        if (qty <= 0) {
            errorLabel.setText("Kuantitas harus lebih besar dari 0.");
            return;
        }

        // Check if already in list
        for (MedicineAllocation alloc : currentAllocations) {
            if (alloc.getMedicineCode().equalsIgnoreCase(selectedMed.getMedicineCode())) {
                // Just update quantity
                alloc.setQuantity(alloc.getQuantity() + qty);
                renderAllocations();
                qtyField.clear();
                errorLabel.setText("");
                return;
            }
        }

        // Add new allocation
        currentAllocations.add(new MedicineAllocation(selectedMed.getMedicineCode(), selectedMed.getMedicineName(), qty, selectedMed.getUnit()));
        renderAllocations();
        qtyField.clear();
        errorLabel.setText("");
    }

    private void renderAllocations() {
        allocationsContainer.getChildren().clear();
        
        boolean isDraft = selectedDistribution == null || "DRAFT".equals(selectedDistribution.getStatus());

        for (MedicineAllocation alloc : currentAllocations) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");

            Label nameLabel = new Label(alloc.getMedicineName() + " (" + alloc.getMedicineCode() + ") x" + alloc.getQuantity() + " " + alloc.getUnit());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-family: 'Inter'; -fx-font-size: 11px;");
            nameLabel.setWrapText(true);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            row.getChildren().add(nameLabel);

            if (isDraft) {
                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ThemeConstants.DANGER + "; -fx-font-weight: bold; -fx-padding: 0 4 0 4; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    currentAllocations.remove(alloc);
                    renderAllocations();
                });
                row.getChildren().add(removeBtn);
            }

            allocationsContainer.getChildren().add(row);
        }
    }

    public static class MedicineAllocation {
        private String medicineCode;
        private String medicineName;
        private int quantity;
        private String unit;

        public MedicineAllocation(String medicineCode, String medicineName, int quantity, String unit) {
            this.medicineCode = medicineCode;
            this.medicineName = medicineName;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getMedicineCode() { return medicineCode; }
        public String getMedicineName() { return medicineName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
    }
}
