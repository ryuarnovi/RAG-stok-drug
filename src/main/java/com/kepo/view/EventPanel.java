package com.kepo.view;

import com.kepo.model.Event;
import com.kepo.model.Shelter;
import com.kepo.model.Refugee;
import com.kepo.service.EventService;
import com.kepo.service.ShelterService;
import com.kepo.service.RefugeeService;
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

public class EventPanel extends VBox implements RefreshablePanel {

    private final EventService eventService;
    private final MainLayout mainLayout;

    // Additional services to calculate relational KPI counts dynamically
    private ShelterService shelterService;
    private RefugeeService refugeeService;

    private Label kpiActiveVal;
    private Label kpiShelterVal;
    private Label kpiRefugeeVal;

    private TextField searchField;
    private FlowPane cardsGrid;

    // Right-hand Drawer
    private VBox drawer;
    private Label drawerTitle;
    private TextField nameField;
    private TextField locationField;
    private ComboBox<String> statusCombo;
    private TextArea descArea;
    private Label errorLabel;
    private Button deleteBtn;

    private Event selectedEvent;

    public EventPanel(EventService eventService, MainLayout mainLayout) {
        this.eventService = eventService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        // --- Title ---
        Label title = new Label("Pusat Kendali Operasi Bencana (Event)");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        getChildren().add(title);

        // --- Row 1: KPI Tiles (3 Cards) ---
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(16);
        kpiGrid.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.3);
            kpiGrid.getColumnConstraints().add(col);
        }

        Pane activeCard = createKpiTile("Event Aktif", kpiActiveVal = new Label("0"), ThemeConstants.PRIMARY);
        Pane shelterCard = createKpiTile("Total Shelter Terlibat", kpiShelterVal = new Label("0"), ThemeConstants.SECONDARY);
        Pane refugeeCard = createKpiTile("Pengungsi Terdata", kpiRefugeeVal = new Label("0"), "#6366f1");

        kpiGrid.add(activeCard, 0, 0);
        kpiGrid.add(shelterCard, 1, 0);
        kpiGrid.add(refugeeCard, 2, 0);
        getChildren().add(kpiGrid);

        // --- Row 2: Search & Create Section ---
        HBox searchRow = new HBox(15);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Cari kejadian bencana...");
        searchField.setStyle(ThemeConstants.INPUT_STYLE);
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newEventBtn = new Button("Tambah Event Baru");
        newEventBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newEventBtn.setOnAction(e -> openDrawerForCreate());

        searchRow.getChildren().addAll(searchField, spacer, newEventBtn);
        getChildren().add(searchRow);

        // --- Row 3: Split Pane (Left: Grid of Cards, Right: Form Drawer) ---
        HBox mainBody = new HBox(20);
        VBox.setVgrow(mainBody, Priority.ALWAYS);

        // Left scrollable Grid
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
        drawer.setPadding(new Insets(20));
        drawer.setStyle(ThemeConstants.CARD_STYLE);
        
        // Drawer content
        drawerTitle = new Label("Detail Event");
        drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        drawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        nameField = new TextField();
        nameField.setPromptText("Nama kejadian bencana");
        nameField.setStyle(ThemeConstants.INPUT_STYLE);

        locationField = new TextField();
        locationField.setPromptText("Lokasi daerah terdampak");
        locationField.setStyle(ThemeConstants.INPUT_STYLE);

        statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "CLOSED"));
        statusCombo.setValue("ACTIVE");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle(ThemeConstants.INPUT_STYLE);

        descArea = new TextArea();
        descArea.setPromptText("Deskripsi operasional tanggap bencana...");
        descArea.setPrefHeight(100);
        descArea.setWrapText(true);
        descArea.setStyle(ThemeConstants.INPUT_STYLE);

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
                createFormLabel("Nama Event"), nameField,
                createFormLabel("Lokasi"), locationField,
                createFormLabel("Status"), statusCombo,
                createFormLabel("Deskripsi"), descArea,
                errorLabel,
                btnRow
        );

        // Hide drawer initially
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

    private Pane createKpiTile(String title, Label valueLabel, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle(ThemeConstants.CARD_STYLE);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        valueLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: 900; -fx-font-size: 24px;");

        Region line = new Region();
        line.setPrefHeight(3);
        line.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 1.5;");

        card.getChildren().addAll(titleLabel, valueLabel, line);
        return card;
    }

    private void renderCards(String query) {
        cardsGrid.getChildren().clear();
        List<Event> events = eventService.getAllEvents();

        // Lazy initialize other services to get counts
        if (shelterService == null) {
            com.kepo.config.DatabaseConfig db = com.kepo.config.DatabaseConfig.getInstance();
            shelterService = new com.kepo.service.ShelterService(new com.kepo.repository.ShelterRepository(db), new com.kepo.service.UserService(new com.kepo.repository.UserRepository(db), new com.kepo.repository.AuditLogRepository(db)));
        }
        if (refugeeService == null) {
            com.kepo.config.DatabaseConfig db = com.kepo.config.DatabaseConfig.getInstance();
            refugeeService = new com.kepo.service.RefugeeService(new com.kepo.repository.RefugeeRepository(db), shelterService, new com.kepo.service.UserService(new com.kepo.repository.UserRepository(db), new com.kepo.repository.AuditLogRepository(db)));
        }

        List<Shelter> allShelters = shelterService.getAllShelters();
        List<Refugee> allRefugees = refugeeService.getAllRefugees();

        // Filter events
        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            events = events.stream()
                    .filter(e -> e.getName().toLowerCase().contains(q) || e.getLocation().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        for (Event e : events) {
            VBox card = new VBox(12);
            card.setPrefWidth(240);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // Title
            Label nameLabel = new Label(e.getName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(208);
            nameLabel.setPrefWidth(208);

            // Location
            Label locLabel = new Label("Lokasi: " + e.getLocation());
            locLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 12px;");
            locLabel.setWrapText(true);
            locLabel.setMaxWidth(208);
            locLabel.setPrefWidth(208);

            // Status Badge
            Label statusBadge = new Label(e.getStatus());
            if ("ACTIVE".equals(e.getStatus())) {
                statusBadge.setStyle(ThemeConstants.BADGE_ACTIVE);
            } else {
                statusBadge.setStyle(ThemeConstants.BADGE_CLOSED);
            }

            // Relationship stats based on location overlap
            long shelterCount = allShelters.stream().filter(s -> 
                s.getLocation().toLowerCase().contains(e.getLocation().toLowerCase()) || 
                e.getLocation().toLowerCase().contains(s.getLocation().toLowerCase())
            ).count();

            long refugeeCount = allRefugees.stream().filter(r -> 
                "CHECKED_IN".equals(r.getStatus()) && allShelters.stream().anyMatch(s -> 
                    s.getShelterId() == r.getShelterId() && (
                        s.getLocation().toLowerCase().contains(e.getLocation().toLowerCase()) || 
                        e.getLocation().toLowerCase().contains(s.getLocation().toLowerCase())
                    )
                )
            ).count();

            HBox statsRow = new HBox(15);
            Label shLabel = new Label(shelterCount + " Shelter");
            shLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label refLabel = new Label(refugeeCount + " Pengungsi");
            refLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold; -fx-font-size: 11px;");

            statsRow.getChildren().addAll(shLabel, refLabel);

            // Action Button
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + "-fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(evt -> openDrawerForEdit(e));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, locLabel, statusBadge, statsRow, footer);
            cardsGrid.getChildren().add(card);
        }
    }

    private void openDrawerForCreate() {
        this.selectedEvent = null;
        drawerTitle.setText("Tambah Event Baru");
        nameField.clear();
        locationField.clear();
        statusCombo.setValue("ACTIVE");
        descArea.clear();
        errorLabel.setText("");
        deleteBtn.setVisible(false);

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void openDrawerForEdit(Event e) {
        this.selectedEvent = e;
        drawerTitle.setText("Edit Detail Event");
        nameField.setText(e.getName());
        locationField.setText(e.getLocation());
        statusCombo.setValue(e.getStatus());
        descArea.setText(e.getDescription());
        errorLabel.setText("");
        deleteBtn.setVisible(true);

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void closeDrawer() {
        drawer.setVisible(false);
        drawer.setManaged(false);
        selectedEvent = null;
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        String status = statusCombo.getValue();
        String desc = descArea.getText().trim();

        if (name.isEmpty() || location.isEmpty()) {
            errorLabel.setText("Nama dan lokasi wajib diisi.");
            return;
        }

        Event e = selectedEvent;
        if (e == null) {
            e = new Event();
        }
        e.setName(name);
        e.setLocation(location);
        e.setStatus(status);
        e.setDescription(desc);

        if (eventService.saveEvent(e)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyimpan data event.");
        }
    }

    private void handleDelete() {
        if (selectedEvent == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus event " + selectedEvent.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (eventService.deleteEvent(selectedEvent.getEventId())) {
                    closeDrawer();
                    refreshData();
                } else {
                    errorLabel.setText("Gagal menghapus event.");
                }
            }
        });
    }

    @Override
    public void refreshData() {
        // Update KPIs
        List<Event> events = eventService.getAllEvents();
        long activeCount = events.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        kpiActiveVal.setText(String.valueOf(activeCount));

        // Re-read relational counts if services exist
        if (shelterService != null) {
            kpiShelterVal.setText(String.valueOf(shelterService.getAllShelters().size()));
        }
        if (refugeeService != null) {
            long activeRefugees = refugeeService.getAllRefugees().stream().filter(r -> "CHECKED_IN".equals(r.getStatus())).count();
            kpiRefugeeVal.setText(String.valueOf(activeRefugees));
        }

        renderCards(searchField.getText());
    }
}
