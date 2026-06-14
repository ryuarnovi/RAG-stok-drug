package com.kepo.view;

import com.kepo.model.Donor;
import com.kepo.model.Supplier;
import com.kepo.service.DonorService;
import com.kepo.service.SupplierService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Node;

import java.util.List;
import java.util.stream.Collectors;

public class SupplierDonorPanel extends TabPane implements RefreshablePanel {

    private final SupplierService supplierService;
    private final DonorService donorService;
    private final MainLayout mainLayout;

    // Supplier UI Components
    private FlowPane sCardsGrid;
    private TextField sSearchField;
    private TextField sNameField;
    private TextField sCpField;
    private TextField sPhoneField;
    private TextField sEmailField;
    private TextArea sAddressArea;
    private Label sErrorLabel;
    private Button sDeleteBtn;
    private VBox sDrawer;
    private Label sDrawerTitle;
    private Supplier selectedSupplier;

    // Donor UI Components
    private FlowPane dCardsGrid;
    private TextField dSearchField;
    private TextField dNameField;
    private TextField dCpField;
    private TextField dPhoneField;
    private TextField dEmailField;
    private TextArea dAddressArea;
    private Label dErrorLabel;
    private Button dDeleteBtn;
    private VBox dDrawer;
    private Label dDrawerTitle;
    private Donor selectedDonor;

    public SupplierDonorPanel(SupplierService supplierService, DonorService donorService, MainLayout mainLayout) {
        this.supplierService = supplierService;
        this.donorService = donorService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        setStyle("-fx-background-color: transparent;");

        // Tab 1: Supplier Directory
        Tab supplierTab = new Tab("Supplier Logistik");
        supplierTab.setContent(buildSupplierContent());

        // Tab 2: Donor Directory
        Tab donorTab = new Tab("Donatur & Sponsor");
        donorTab.setContent(buildDonorContent());

        getTabs().addAll(supplierTab, donorTab);
        
        refreshData();
    }

    private Node buildSupplierContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: transparent;");

        // Top Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Direktori Supplier Logistik");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        sSearchField = new TextField();
        sSearchField.setPromptText("Cari supplier...");
        sSearchField.setStyle(ThemeConstants.INPUT_STYLE);
        sSearchField.setPrefWidth(220);
        sSearchField.textProperty().addListener((obs, oldVal, newVal) -> renderSupplierCards(newVal));

        Button newSupplierBtn = new Button("Tambah Supplier");
        newSupplierBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newSupplierBtn.setOnAction(e -> openSupplierDrawerForCreate());

        header.getChildren().addAll(title, spacer, sSearchField, newSupplierBtn);
        container.getChildren().add(header);

        // Main Body Layout
        HBox body = new HBox(15);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Scrollable Cards Grid
        sCardsGrid = new FlowPane();
        sCardsGrid.setHgap(16);
        sCardsGrid.setVgap(16);
        sCardsGrid.setPadding(new Insets(2));

        ScrollPane gridScroll = new ScrollPane(sCardsGrid);
        HBox.setHgrow(gridScroll, Priority.ALWAYS);
        gridScroll.setFitToWidth(true);
        gridScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Right Drawer Panel
        sDrawer = new VBox(12);
        sDrawer.setPrefWidth(320);
        sDrawer.setPadding(new Insets(15));
        sDrawer.setStyle(ThemeConstants.CARD_STYLE);

        sDrawerTitle = new Label("Detail Data Supplier");
        sDrawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 14));
        sDrawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        sNameField = new TextField();
        sNameField.setPromptText("Nama perusahaan / badan");
        sNameField.setStyle(ThemeConstants.INPUT_STYLE);

        sCpField = new TextField();
        sCpField.setPromptText("Nama perwakilan supplier");
        sCpField.setStyle(ThemeConstants.INPUT_STYLE);

        sPhoneField = new TextField();
        sPhoneField.setPromptText("Nomor telp kantor");
        sPhoneField.setStyle(ThemeConstants.INPUT_STYLE);

        sEmailField = new TextField();
        sEmailField.setPromptText("kontak@supplier.co.id");
        sEmailField.setStyle(ThemeConstants.INPUT_STYLE);

        sAddressArea = new TextArea();
        sAddressArea.setPromptText("Alamat lengkap kantor pusat");
        sAddressArea.setPrefHeight(60);
        sAddressArea.setWrapText(true);
        sAddressArea.setStyle(ThemeConstants.INPUT_STYLE);

        sErrorLabel = new Label();
        sErrorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

        HBox sBtnRow = new HBox(8);
        Button sSaveBtn = new Button("Simpan");
        sSaveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        sSaveBtn.setOnAction(e -> handleSaveSupplier());

        Button sResetBtn = new Button("Tutup");
        sResetBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        sResetBtn.setOnAction(e -> closeSupplierDrawer());

        sDeleteBtn = new Button("Hapus");
        sDeleteBtn.setStyle(ThemeConstants.DANGER_BTN_STYLE);
        sDeleteBtn.setOnAction(e -> handleDeleteSupplier());
        sBtnRow.getChildren().addAll(sSaveBtn, sResetBtn, sDeleteBtn);

        sDrawer.getChildren().addAll(
                sDrawerTitle, new Separator(),
                createFormLabel("Nama Supplier"), sNameField,
                createFormLabel("Kontak Person"), sCpField,
                createFormLabel("No Telepon"), sPhoneField,
                createFormLabel("Email"), sEmailField,
                createFormLabel("Alamat"), sAddressArea,
                sErrorLabel, sBtnRow
        );

        closeSupplierDrawer();

        body.getChildren().addAll(gridScroll, sDrawer);
        container.getChildren().add(body);
        return container;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        return label;
    }

    private Node buildDonorContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: transparent;");

        // Top Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Direktori Donatur & Sponsor");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dSearchField = new TextField();
        dSearchField.setPromptText("Cari donatur...");
        dSearchField.setStyle(ThemeConstants.INPUT_STYLE);
        dSearchField.setPrefWidth(220);
        dSearchField.textProperty().addListener((obs, oldVal, newVal) -> renderDonorCards(newVal));

        Button newDonorBtn = new Button("Tambah Donatur");
        newDonorBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newDonorBtn.setOnAction(e -> openDonorDrawerForCreate());

        header.getChildren().addAll(title, spacer, dSearchField, newDonorBtn);
        container.getChildren().add(header);

        // Main Body Layout
        HBox body = new HBox(15);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Scrollable Cards Grid
        dCardsGrid = new FlowPane();
        dCardsGrid.setHgap(16);
        dCardsGrid.setVgap(16);
        dCardsGrid.setPadding(new Insets(2));

        ScrollPane gridScroll = new ScrollPane(dCardsGrid);
        HBox.setHgrow(gridScroll, Priority.ALWAYS);
        gridScroll.setFitToWidth(true);
        gridScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Right Drawer Panel
        dDrawer = new VBox(12);
        dDrawer.setPrefWidth(320);
        dDrawer.setPadding(new Insets(15));
        dDrawer.setStyle(ThemeConstants.CARD_STYLE);

        dDrawerTitle = new Label("Detail Data Donatur");
        dDrawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 14));
        dDrawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        dNameField = new TextField();
        dNameField.setPromptText("Nama donatur / NGO / Korporat");
        dNameField.setStyle(ThemeConstants.INPUT_STYLE);

        dCpField = new TextField();
        dCpField.setPromptText("Nama perwakilan");
        dCpField.setStyle(ThemeConstants.INPUT_STYLE);

        dPhoneField = new TextField();
        dPhoneField.setPromptText("No telp kontak");
        dPhoneField.setStyle(ThemeConstants.INPUT_STYLE);

        dEmailField = new TextField();
        dEmailField.setPromptText("donasi@mitra.org");
        dEmailField.setStyle(ThemeConstants.INPUT_STYLE);

        dAddressArea = new TextArea();
        dAddressArea.setPromptText("Alamat kantor / domisili donatur");
        dAddressArea.setPrefHeight(60);
        dAddressArea.setWrapText(true);
        dAddressArea.setStyle(ThemeConstants.INPUT_STYLE);

        dErrorLabel = new Label();
        dErrorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

        HBox dBtnRow = new HBox(8);
        Button dSaveBtn = new Button("Simpan");
        dSaveBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        dSaveBtn.setOnAction(e -> handleSaveDonor());

        Button dResetBtn = new Button("Tutup");
        dResetBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE);
        dResetBtn.setOnAction(e -> closeDonorDrawer());

        dDeleteBtn = new Button("Hapus");
        dDeleteBtn.setStyle(ThemeConstants.DANGER_BTN_STYLE);
        dDeleteBtn.setOnAction(e -> handleDeleteDonor());
        dBtnRow.getChildren().addAll(dSaveBtn, dResetBtn, dDeleteBtn);

        dDrawer.getChildren().addAll(
                dDrawerTitle, new Separator(),
                createFormLabel("Nama Donatur"), dNameField,
                createFormLabel("Nama Kontak"), dCpField,
                createFormLabel("No Telepon"), dPhoneField,
                createFormLabel("Email"), dEmailField,
                createFormLabel("Alamat"), dAddressArea,
                dErrorLabel, dBtnRow
        );

        closeDonorDrawer();

        body.getChildren().addAll(gridScroll, dDrawer);
        container.getChildren().add(body);
        return container;
    }

    private void renderSupplierCards(String query) {
        sCardsGrid.getChildren().clear();
        List<Supplier> suppliers = supplierService.getAllSuppliers();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            suppliers = suppliers.stream()
                    .filter(s -> s.getSupplierName().toLowerCase().contains(q) ||
                            (s.getContactPerson() != null && s.getContactPerson().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        for (Supplier s : suppliers) {
            VBox card = new VBox(8);
            card.setPrefWidth(240);
            card.setPadding(new Insets(14));
            card.setStyle(ThemeConstants.CARD_STYLE);

            Label nameLabel = new Label(s.getSupplierName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(208);
            nameLabel.setPrefWidth(208);

            Label cpLabel = new Label("CP: " + (s.getContactPerson() != null ? s.getContactPerson() : "-"));
            cpLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            cpLabel.setWrapText(true);
            cpLabel.setMaxWidth(208);
            cpLabel.setPrefWidth(208);

            Label phoneLabel = new Label("Telp: " + (s.getPhone() != null ? s.getPhone() : "-"));
            phoneLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");

            Label emailLabel = new Label("Email: " + (s.getEmail() != null ? s.getEmail() : "-"));
            emailLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");

            Label addrLabel = new Label("Alamat: " + (s.getAddress() != null && !s.getAddress().isBlank() ? s.getAddress() : "-"));
            addrLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            addrLabel.setWrapText(true);
            addrLabel.setMaxWidth(208);
            addrLabel.setPrefWidth(208);

            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + " -fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(evt -> openSupplierDrawerForEdit(s));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, cpLabel, phoneLabel, emailLabel, addrLabel, footer);
            sCardsGrid.getChildren().add(card);
        }
    }

    private void renderDonorCards(String query) {
        dCardsGrid.getChildren().clear();
        List<Donor> donors = donorService.getAllDonors();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            donors = donors.stream()
                    .filter(d -> d.getDonorName().toLowerCase().contains(q) ||
                            (d.getContact() != null && d.getContact().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        for (Donor d : donors) {
            VBox card = new VBox(8);
            card.setPrefWidth(240);
            card.setPadding(new Insets(14));
            card.setStyle(ThemeConstants.CARD_STYLE);

            Label nameLabel = new Label(d.getDonorName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(208);
            nameLabel.setPrefWidth(208);

            Label cpLabel = new Label("Kontak: " + (d.getContact() != null ? d.getContact() : "-"));
            cpLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            cpLabel.setWrapText(true);
            cpLabel.setMaxWidth(208);
            cpLabel.setPrefWidth(208);

            Label phoneLabel = new Label("Telp: " + (d.getPhone() != null ? d.getPhone() : "-"));
            phoneLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");

            Label emailLabel = new Label("Email: " + (d.getEmail() != null ? d.getEmail() : "-"));
            emailLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");

            Label addrLabel = new Label("Alamat: " + (d.getAddress() != null && !d.getAddress().isBlank() ? d.getAddress() : "-"));
            addrLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-size: 11px;");
            addrLabel.setWrapText(true);
            addrLabel.setMaxWidth(208);
            addrLabel.setPrefWidth(208);

            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + " -fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(evt -> openDonorDrawerForEdit(d));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(nameLabel, cpLabel, phoneLabel, emailLabel, addrLabel, footer);
            dCardsGrid.getChildren().add(card);
        }
    }

    private void openSupplierDrawerForCreate() {
        selectedSupplier = null;
        sDrawerTitle.setText("Tambah Supplier Baru");
        sNameField.clear();
        sCpField.clear();
        sPhoneField.clear();
        sEmailField.clear();
        sAddressArea.clear();
        sErrorLabel.setText("");
        sDeleteBtn.setVisible(false);

        sDrawer.setVisible(true);
        sDrawer.setManaged(true);
    }

    private void openSupplierDrawerForEdit(Supplier s) {
        selectedSupplier = s;
        sDrawerTitle.setText("Edit Data Supplier");
        sNameField.setText(s.getSupplierName());
        sCpField.setText(s.getContactPerson());
        sPhoneField.setText(s.getPhone());
        sEmailField.setText(s.getEmail());
        sAddressArea.setText(s.getAddress());
        sErrorLabel.setText("");
        sDeleteBtn.setVisible(true);

        sDrawer.setVisible(true);
        sDrawer.setManaged(true);
    }

    private void closeSupplierDrawer() {
        sDrawer.setVisible(false);
        sDrawer.setManaged(false);
        selectedSupplier = null;
    }

    private void openDonorDrawerForCreate() {
        selectedDonor = null;
        dDrawerTitle.setText("Tambah Donatur Baru");
        dNameField.clear();
        dCpField.clear();
        dPhoneField.clear();
        dEmailField.clear();
        dAddressArea.clear();
        dErrorLabel.setText("");
        dDeleteBtn.setVisible(false);

        dDrawer.setVisible(true);
        dDrawer.setManaged(true);
    }

    private void openDonorDrawerForEdit(Donor d) {
        selectedDonor = d;
        dDrawerTitle.setText("Edit Data Donatur");
        dNameField.setText(d.getDonorName());
        dCpField.setText(d.getContact());
        dPhoneField.setText(d.getPhone());
        dEmailField.setText(d.getEmail());
        dAddressArea.setText(d.getAddress());
        dErrorLabel.setText("");
        dDeleteBtn.setVisible(true);

        dDrawer.setVisible(true);
        dDrawer.setManaged(true);
    }

    private void closeDonorDrawer() {
        dDrawer.setVisible(false);
        dDrawer.setManaged(false);
        selectedDonor = null;
    }

    private void handleSaveSupplier() {
        String name = sNameField.getText().trim();
        String cp = sCpField.getText().trim();
        String phone = sPhoneField.getText().trim();
        String email = sEmailField.getText().trim();
        String addr = sAddressArea.getText().trim();

        if (name.isEmpty()) {
            sErrorLabel.setText("Nama supplier harus diisi.");
            return;
        }

        Supplier s = selectedSupplier;
        if (s == null) {
            s = new Supplier();
        }
        s.setSupplierName(name);
        s.setContactPerson(cp);
        s.setPhone(phone);
        s.setEmail(email);
        s.setAddress(addr);

        if (supplierService.saveSupplier(s)) {
            closeSupplierDrawer();
            refreshData();
        } else {
            sErrorLabel.setText("Gagal menyimpan supplier.");
        }
    }

    private void handleSaveDonor() {
        String name = dNameField.getText().trim();
        String cp = dCpField.getText().trim();
        String phone = dPhoneField.getText().trim();
        String email = dEmailField.getText().trim();
        String addr = dAddressArea.getText().trim();

        if (name.isEmpty()) {
            dErrorLabel.setText("Nama donatur harus diisi.");
            return;
        }

        Donor d = selectedDonor;
        if (d == null) {
            d = new Donor();
        }
        d.setDonorName(name);
        d.setContact(cp);
        d.setPhone(phone);
        d.setEmail(email);
        d.setAddress(addr);

        if (donorService.saveDonor(d)) {
            closeDonorDrawer();
            refreshData();
        } else {
            dErrorLabel.setText("Gagal menyimpan donatur.");
        }
    }

    private void handleDeleteSupplier() {
        if (selectedSupplier == null) {
            sErrorLabel.setText("Pilih supplier yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus supplier " + selectedSupplier.getSupplierName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (supplierService.deleteSupplier(selectedSupplier.getSupplierId())) {
                    closeSupplierDrawer();
                    refreshData();
                } else {
                    sErrorLabel.setText("Gagal menghapus supplier.");
                }
            }
        });
    }

    private void handleDeleteDonor() {
        if (selectedDonor == null) {
            dErrorLabel.setText("Pilih donatur yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus donatur " + selectedDonor.getDonorName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (donorService.deleteDonor(selectedDonor.getDonorId())) {
                    closeDonorDrawer();
                    refreshData();
                } else {
                    dErrorLabel.setText("Gagal menghapus donatur.");
                }
            }
        });
    }

    @Override
    public void refreshData() {
        // Refresh Supplier Cards
        renderSupplierCards(sSearchField != null ? sSearchField.getText() : null);

        // Refresh Donor Cards
        renderDonorCards(dSearchField != null ? dSearchField.getText() : null);
    }
}
