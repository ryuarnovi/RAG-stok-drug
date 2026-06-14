package com.kepo.view;

import com.kepo.controller.InventoryController;
import com.kepo.model.Medicine;
import com.kepo.model.Supplier;
import com.kepo.service.SupplierService;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.image.BufferedImage;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MedicinePanel extends VBox implements RefreshablePanel {

    private final InventoryController controller;
    private final SupplierService supplierService;
    private final MainLayout mainLayout;

    private TextField searchField;
    private FlowPane cardsGrid;

    // Right-hand Drawer
    private VBox drawer;
    private Label drawerTitle;
    private TextField codeField;
    private TextField nameField;
    private TextField categoryField;
    private TextField batchField;
    private TextField unitField;
    private TextField stockField;
    private TextField minStockField;
    private TextField buyPriceField;
    private TextField sellPriceField;
    private DatePicker expiryPicker;
    private ComboBox<Supplier> supplierCombo;
    private Label errorLabel;
    
    private ImageView barcodeImageView;
    private Button deleteBtn;
    private Button adjustBtn;

    private Medicine selectedMedicine;

    public MedicinePanel(InventoryController controller, SupplierService supplierService, MainLayout mainLayout) {
        this.controller = controller;
        this.supplierService = supplierService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        // --- Header Row ---
        Label title = new Label("Inventaris Obat & Logistik Kesehatan");
        title.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.ON_SURFACE));
        getChildren().add(title);

        // --- Search & Action Row ---
        HBox searchRow = new HBox(15);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Cari nama obat atau kategori...");
        searchField.setStyle(ThemeConstants.INPUT_STYLE);
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newMedBtn = new Button("Tambah Obat Baru");
        newMedBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        newMedBtn.setOnAction(e -> openDrawerForCreate());

        searchRow.getChildren().addAll(searchField, spacer, newMedBtn);
        getChildren().add(searchRow);

        // --- Main Split Content ---
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
        drawer = new VBox(10);
        drawer.setPrefWidth(350);
        drawer.setPadding(new Insets(20));
        drawer.setStyle(ThemeConstants.CARD_STYLE);

        // Scrollpane for form inside drawer to prevent overflow
        ScrollPane formScroll = new ScrollPane();
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        VBox scrollContent = new VBox(8);
        scrollContent.setStyle("-fx-background-color: transparent;");

        drawerTitle = new Label("Detail Logistik Obat");
        drawerTitle.setFont(Font.font("Plus Jakarta Sans", FontWeight.BOLD, 15));
        drawerTitle.setTextFill(Color.web(ThemeConstants.ON_SURFACE));

        // Form Fields
        codeField = new TextField();
        codeField.setPromptText("MED-000");
        codeField.setStyle(ThemeConstants.INPUT_STYLE);

        nameField = new TextField();
        nameField.setPromptText("Nama obat / logistik");
        nameField.setStyle(ThemeConstants.INPUT_STYLE);

        categoryField = new TextField();
        categoryField.setPromptText("Kategori obat");
        categoryField.setStyle(ThemeConstants.INPUT_STYLE);

        batchField = new TextField();
        batchField.setPromptText("Nomor Batch");
        batchField.setStyle(ThemeConstants.INPUT_STYLE);

        unitField = new TextField();
        unitField.setPromptText("Tablet / Kapsul / Botol");
        unitField.setStyle(ThemeConstants.INPUT_STYLE);

        stockField = new TextField();
        stockField.setPromptText("Stok awal");
        stockField.setStyle(ThemeConstants.INPUT_STYLE);

        minStockField = new TextField();
        minStockField.setPromptText("Batas stok minimum");
        minStockField.setStyle(ThemeConstants.INPUT_STYLE);

        buyPriceField = new TextField();
        buyPriceField.setPromptText("Harga Beli");
        buyPriceField.setStyle(ThemeConstants.INPUT_STYLE);

        sellPriceField = new TextField();
        sellPriceField.setPromptText("Harga Jual");
        sellPriceField.setStyle(ThemeConstants.INPUT_STYLE);

        expiryPicker = new DatePicker();
        expiryPicker.setMaxWidth(Double.MAX_VALUE);
        expiryPicker.setStyle(ThemeConstants.INPUT_STYLE);

        supplierCombo = new ComboBox<>();
        supplierCombo.setMaxWidth(Double.MAX_VALUE);
        supplierCombo.setStyle(ThemeConstants.INPUT_STYLE);

        // Barcode Preview Box
        VBox barcodeBox = new VBox(5);
        barcodeBox.setAlignment(Pos.CENTER);
        Label barcodeLabel = new Label("Barcode Preview:");
        barcodeLabel.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        barcodeImageView = new ImageView();
        barcodeImageView.setFitWidth(150);
        barcodeImageView.setFitHeight(40);
        barcodeBox.getChildren().addAll(barcodeLabel, barcodeImageView);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ThemeConstants.DANGER));

        HBox btnRow = new HBox(6);
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

        adjustBtn = new Button("Sesuaikan Stok (IN/OUT)");
        adjustBtn.setMaxWidth(Double.MAX_VALUE);
        adjustBtn.setStyle(ThemeConstants.SECONDARY_BTN_STYLE);
        adjustBtn.setOnAction(e -> showAdjustStockDialog());

        scrollContent.getChildren().addAll(
                drawerTitle,
                createFormLabel("Kode"), codeField,
                createFormLabel("Nama Obat"), nameField,
                createFormLabel("Kategori"), categoryField,
                createFormLabel("Nomor Batch"), batchField,
                createFormLabel("Unit"), unitField,
                createFormLabel("Stok Awal"), stockField,
                createFormLabel("Minimum Stok"), minStockField,
                createFormLabel("Harga Beli"), buyPriceField,
                createFormLabel("Harga Jual"), sellPriceField,
                createFormLabel("Tanggal Kadaluarsa"), expiryPicker,
                createFormLabel("Pilih Supplier"), supplierCombo,
                new Separator(),
                barcodeBox,
                new Separator(),
                adjustBtn,
                errorLabel,
                btnRow
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
        List<Medicine> list = controller.getAllMedicines();

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            list = list.stream()
                    .filter(m -> m.getMedicineName().toLowerCase().contains(q) || m.getCategory().toLowerCase().contains(q) || m.getMedicineCode().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        for (Medicine m : list) {
            VBox card = new VBox(10);
            card.setPrefWidth(240);
            card.setPadding(new Insets(16));
            card.setStyle(ThemeConstants.CARD_STYLE);

            // Code
            Label codeLabel = new Label(m.getMedicineCode());
            codeLabel.setStyle("-fx-text-fill: " + ThemeConstants.PRIMARY_LIGHT + "; -fx-font-weight: bold; -fx-font-size: 10px;");

            // Name
            Label nameLabel = new Label(m.getMedicineName());
            nameLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: bold; -fx-font-size: 15px;");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(208);
            nameLabel.setPrefWidth(208);

            // Stock Count
            Label qtyLabel = new Label(m.getStockQuantity() + " " + m.getUnit());
            qtyLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE + "; -fx-font-weight: 900; -fx-font-size: 18px;");

            // Expiry Date (Highlight red if near-expiry)
            boolean nearExpiry = false;
            String expText = "EXP: ";
            if (m.getExpiryDate() != null) {
                LocalDate expLocalDate = m.getExpiryDate().toLocalDate();
                nearExpiry = expLocalDate.isBefore(LocalDate.now().plusDays(30));
                expText += m.getExpiryDate().toString();
            } else {
                expText += "N/A";
            }

            Label expLabel = new Label(expText);
            expLabel.setWrapText(true);
            expLabel.setMaxWidth(208);
            expLabel.setPrefWidth(208);
            if (nearExpiry) {
                expLabel.setStyle("-fx-text-fill: " + ThemeConstants.DANGER + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                expLabel.setStyle("-fx-text-fill: " + ThemeConstants.ON_SURFACE_VARIANT + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            }

            // Status chip (IN STOCK, LOW STOCK, OUT OF STOCK)
            Label badge = new Label();
            if (m.getStockQuantity() == 0) {
                badge.setText("OUT OF STOCK");
                badge.setStyle(ThemeConstants.BADGE_CRITICAL);
            } else if (m.getStockQuantity() <= m.getMinimumStock()) {
                badge.setText("LOW STOCK");
                badge.setStyle(ThemeConstants.BADGE_WARNING);
            } else {
                badge.setText("IN STOCK");
                badge.setStyle(ThemeConstants.BADGE_SAFE);
            }

            // Miniature Barcode preview
            ImageView miniBarcode = new ImageView();
            miniBarcode.setFitWidth(100);
            miniBarcode.setFitHeight(25);
            BufferedImage bi = controller.generateBarcode(m.getMedicineCode());
            if (bi != null) {
                miniBarcode.setImage(SwingFXUtils.toFXImage(bi, null));
            }

            HBox badgeAndBarcode = new HBox(10, badge, miniBarcode);
            badgeAndBarcode.setAlignment(Pos.CENTER_LEFT);

            // Details Button
            Button detailsBtn = new Button("Detail");
            detailsBtn.setStyle(ThemeConstants.OUTLINE_BTN_STYLE + "-fx-padding: 4 12 4 12;");
            detailsBtn.setOnAction(e -> openDrawerForEdit(m));

            HBox footer = new HBox(detailsBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(codeLabel, nameLabel, qtyLabel, expLabel, badgeAndBarcode, footer);
            cardsGrid.getChildren().add(card);
        }
    }

    private void openDrawerForCreate() {
        selectedMedicine = null;
        drawerTitle.setText("Registrasi Obat Baru");
        codeField.clear();
        codeField.setDisable(false);
        nameField.clear();
        categoryField.clear();
        batchField.clear();
        unitField.clear();
        stockField.clear();
        stockField.setDisable(false);
        minStockField.clear();
        buyPriceField.clear();
        sellPriceField.clear();
        expiryPicker.setValue(null);
        supplierCombo.setValue(null);
        barcodeImageView.setImage(null);
        errorLabel.setText("");

        deleteBtn.setVisible(false);
        adjustBtn.setVisible(false);

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void openDrawerForEdit(Medicine m) {
        selectedMedicine = m;
        drawerTitle.setText("Edit Detail Logistik");
        codeField.setText(m.getMedicineCode());
        codeField.setDisable(true);
        nameField.setText(m.getMedicineName());
        categoryField.setText(m.getCategory());
        batchField.setText(m.getBatchNumber());
        unitField.setText(m.getUnit());
        stockField.setText(String.valueOf(m.getStockQuantity()));
        stockField.setDisable(true); // Must use Adjust Stock dialog
        minStockField.setText(String.valueOf(m.getMinimumStock()));
        buyPriceField.setText(String.valueOf(m.getPurchasePrice()));
        sellPriceField.setText(String.valueOf(m.getSellingPrice()));

        if (m.getExpiryDate() != null) {
            expiryPicker.setValue(m.getExpiryDate().toLocalDate());
        } else {
            expiryPicker.setValue(null);
        }

        // Set supplier
        if (m.getSupplierId() != null) {
            for (Supplier s : supplierCombo.getItems()) {
                if (s.getSupplierId() == m.getSupplierId()) {
                    supplierCombo.setValue(s);
                    break;
                }
            }
        } else {
            supplierCombo.setValue(null);
        }

        // Generate Barcode preview
        BufferedImage bi = controller.generateBarcode(m.getMedicineCode());
        if (bi != null) {
            barcodeImageView.setImage(SwingFXUtils.toFXImage(bi, null));
        } else {
            barcodeImageView.setImage(null);
        }

        errorLabel.setText("");
        deleteBtn.setVisible(true);
        adjustBtn.setVisible(true);

        drawer.setVisible(true);
        drawer.setManaged(true);
    }

    private void closeDrawer() {
        drawer.setVisible(false);
        drawer.setManaged(false);
        selectedMedicine = null;
    }

    private void handleSave() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String cat = categoryField.getText().trim();
        String batch = batchField.getText().trim();
        String unit = unitField.getText().trim();
        String stockStr = stockField.getText().trim();
        String minStr = minStockField.getText().trim();
        String buyStr = buyPriceField.getText().trim();
        String sellStr = sellPriceField.getText().trim();
        LocalDate exp = expiryPicker.getValue();
        Supplier supplier = supplierCombo.getValue();

        if (code.isEmpty() || name.isEmpty() || cat.isEmpty() || unit.isEmpty() || minStr.isEmpty()) {
            errorLabel.setText("Kode, Nama, Kategori, Unit, dan Min Stok wajib diisi.");
            return;
        }

        int stock = 0;
        int minStock;
        double buyPrice = 0.0;
        double sellPrice = 0.0;

        try {
            if (selectedMedicine == null) {
                stock = Integer.parseInt(stockStr);
            }
            minStock = Integer.parseInt(minStr);
            if (!buyStr.isEmpty()) buyPrice = Double.parseDouble(buyStr);
            if (!sellStr.isEmpty()) sellPrice = Double.parseDouble(sellStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Input angka untuk Stok, Min Stok, dan Harga tidak valid.");
            return;
        }

        Medicine m = selectedMedicine;
        if (m == null) {
            m = new Medicine();
            m.setStockQuantity(stock);
        }
        m.setMedicineCode(code);
        m.setMedicineName(name);
        m.setCategory(cat);
        m.setBatchNumber(batch);
        m.setUnit(unit);
        m.setMinimumStock(minStock);
        m.setPurchasePrice(buyPrice);
        m.setSellingPrice(sellPrice);
        m.setExpiryDate(exp != null ? Date.valueOf(exp) : null);
        m.setSupplierId(supplier != null ? supplier.getSupplierId() : null);

        if (controller.saveMedicine(m)) {
            closeDrawer();
            refreshData();
        } else {
            errorLabel.setText("Gagal menyimpan obat. Kode mungkin duplikat.");
        }
    }

    private void handleDelete() {
        if (selectedMedicine == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus obat " + selectedMedicine.getMedicineName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (controller.deleteMedicine(selectedMedicine.getMedicineId())) {
                    closeDrawer();
                    refreshData();
                } else {
                    errorLabel.setText("Gagal menghapus obat.");
                }
            }
        });
    }

    private void showAdjustStockDialog() {
        if (selectedMedicine == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sesuaikan Stok Obat");
        dialog.setHeaderText("Sesuaikan persediaan: " + selectedMedicine.getMedicineName());

        ButtonType saveButtonType = new ButtonType("Terapkan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("IN (Stok Masuk / Tambahan)", "OUT (Penggunaan Darurat)", "ADJUSTMENT (Koreksi Opname)"));
        typeCombo.setValue("IN (Stok Masuk / Tambahan)");
        typeCombo.setStyle(ThemeConstants.INPUT_STYLE);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Jumlah qty");
        qtyField.setStyle(ThemeConstants.INPUT_STYLE);

        TextField notesField = new TextField();
        notesField.setPromptText("Catatan pengiriman/alasan koreksi");
        notesField.setStyle(ThemeConstants.INPUT_STYLE);

        grid.add(new Label("Tipe Penyesuaian:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Jumlah Qty:"), 0, 1);
        grid.add(qtyField, 1, 1);
        grid.add(new Label("Catatan / Notes:"), 0, 2);
        grid.add(notesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    int qty = Integer.parseInt(qtyField.getText().trim());
                    String type = typeCombo.getValue();
                    String notes = notesField.getText().trim();

                    boolean success = false;
                    if (type.startsWith("IN")) {
                        success = controller.addStock(selectedMedicine.getMedicineId(), qty, notes);
                    } else if (type.startsWith("OUT")) {
                        success = controller.reduceStock(selectedMedicine.getMedicineId(), qty, notes);
                    } else {
                        success = controller.adjustStock(selectedMedicine.getMedicineId(), qty, notes);
                    }

                    if (success) {
                        closeDrawer();
                        refreshData();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal memperbarui stok database.");
                        alert.showAndWait();
                    }

                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Jumlah Qty harus berupa angka bulat positif.");
                    alert.showAndWait();
                }
            }
        });
    }

    @Override
    public void refreshData() {
        // Refresh supplier dropdown
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        supplierCombo.setItems(FXCollections.observableArrayList(suppliers));

        renderCards(searchField.getText());
    }
}
