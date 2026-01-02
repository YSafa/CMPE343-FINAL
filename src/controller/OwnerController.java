package controller;

import dao.RatingDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserDAO;
import dao.CouponDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Coupon;
import model.Order;
import model.Product;
import model.User;
import util.Alertutil;

import javafx.scene.layout.VBox;


import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;

import database.DatabaseConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;

import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;



public class OwnerController {

    // --- FXML UI Elements ---
    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice, colProdStock, colProdThreshold;
    
    @FXML private TextField txtProductName, txtProductPrice, txtProductStock, txtProductThreshold;
    @FXML private ComboBox<String> comboProductType;

    @FXML private TableView<User> tableCarriers;
    @FXML private TableColumn<User, Integer> colCarrId;
    @FXML private TableColumn<User, String> colCarrUser, colCarrAddress;
    @FXML private TableColumn<User, String> colCarrRating;

    @FXML private TextField txtCarrierUser, txtCarrierAddress;
    @FXML private PasswordField txtCarrierPass;

    @FXML private TableView<Order> tableOrders;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, Timestamp> colOrderTime;
    @FXML private TableColumn<Order, String> colOrderCustomer, colOrderCarrier, colOrderStatus, colOrderContent;
    @FXML private TableColumn<Order, Double> colOrderTotal;

    @FXML private Label lblTotalRevenue, lblTotalOrders, lblActiveCarriers;
    @FXML private BarChart<String, Number> salesChart;
    @FXML private PieChart statusPieChart; // YENİ EKLENDİ
    @FXML private ComboBox<User> customerBox;

    //kupon için
    @FXML private TableView<Coupon> tableCoupons;
    @FXML private TableColumn<Coupon, Integer> colCouponId;
    @FXML private TableColumn<Coupon, String> colCouponCode, colCouponExpire;
    @FXML private TableColumn<Coupon, Double> colCouponRate;
    @FXML private TableColumn<Coupon, Boolean> colCouponActive;

    @FXML private TextField txtCouponCode, txtCouponRate, txtCouponDays, txtCouponMin;

    @FXML private javafx.scene.image.ImageView productImageView;
    @FXML private Label imageInfoLabel;

    private byte[] selectedImageBytes; // seçilen / mevcut resim



    // --- DAOs ---
    private ProductDAO productDAO = new ProductDAO();
    private UserDAO userDAO = new UserDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private CouponDAO couponDAO = new CouponDAO();
    private RatingDAO ratingDAO = new RatingDAO();

    @FXML
    public void initialize() {
        loadCustomers();
        setupTableColumns();
        setupCouponColumns();
        loadAllData();
        handleRefreshCoupons();
        if (comboProductType != null) {
            comboProductType.setItems(FXCollections.observableArrayList("vegetable", "fruit"));
        }

        productImageView.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles() && isImageFile(db.getFiles().get(0))) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        productImageView.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                File f = db.getFiles().get(0);
                if (isImageFile(f)) {
                    loadImageFromFile(f);
                } else {
                    imageInfoLabel.setText("Please drop an image file.");
                }
            }
            e.setDropCompleted(true);
            e.consume();
        });

        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP == null) return;

            txtProductName.setText(newP.getName());
            txtProductPrice.setText(String.valueOf(newP.getPrice()));
            txtProductStock.setText(String.valueOf(newP.getStock()));
            txtProductThreshold.setText(String.valueOf(newP.getThreshold()));
            comboProductType.setValue(newP.getType());

            byte[] img = newP.getImage();
            if (img != null && img.length > 0) {
                productImageView.setImage(new Image(new ByteArrayInputStream(img)));
                selectedImageBytes = img;
            } else {
                productImageView.setImage(null);
                selectedImageBytes = null;
            }
        });

    }


    @FXML
    private void handleViewCarrierReviews() {

        User selected = tableCarriers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alertutil.showWarningMessage("Please select a carrier.");
            return;
        }

        List<String> comments =
                ratingDAO.getCommentsForCarrier(selected.getId());

        if (comments.isEmpty()) {
            Alertutil.showInfoMessage("No comments for this carrier.");
            return;
        }

        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);

        comments.forEach(c -> area.appendText("• " + c + "\n\n"));

        Stage stage = new Stage();
        stage.setTitle("Carrier Reviews");

        stage.setScene(new Scene(new VBox(area), 450, 300));
        stage.show();
    }

    private void setupTableColumns() {
        // Product Table
        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProdStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProdThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));

        // Carrier Table
        colCarrId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCarrUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCarrAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        colCarrRating.setCellValueFactory(cell -> {
            User carrier = cell.getValue();

            double avg = ratingDAO.getAverageRatingForCarrier(carrier.getId());
            int count = ratingDAO.getRatingCountForCarrier(carrier.getId());
            int commentCount = ratingDAO.getCommentsForCarrier(carrier.getId()).size();

            if (count == 0) {
                return new SimpleStringProperty("No ratings");
            }

            String commentIcon = commentCount > 0 ? " 💬" : "";

            return new SimpleStringProperty(
                    String.format("⭐ %.1f (%d)%s", avg, count, commentIcon)
            );
        });


        // Order Table
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colOrderCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colOrderCarrier.setCellValueFactory(new PropertyValueFactory<>("carrierName"));
        colOrderContent.setCellValueFactory(new PropertyValueFactory<>("products"));
        colOrderStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus())
        );
    }

    private void loadAllData() {
        refreshProductTable();
        refreshCarrierTable();
        refreshOrderTable();
        updateReports(); // Özet kartları ve grafikleri günceller
    }

    // --- REPORT LOGIC (DASHBOARD) ---
    private void updateReports() {
        List<Order> allOrders = orderDAO.getAllOrdersWithDetails();
        List<User> carriers = userDAO.getUsersByRole("carrier");

        double totalRev = 0;
        int delivered = 0, pending = 0, cancelled = 0;

        // İstatistikleri Hesapla
        for (Order o : allOrders) {
            if (o.isCancelled()) {
                cancelled++;
            } else if (o.isDelivered()) {
                delivered++;
                totalRev += o.getTotalCost();
            } else {
                pending++;
            }
        }

        // Kartları Güncelle
        lblTotalRevenue.setText(String.format("₺ %.2f", totalRev));
        lblTotalOrders.setText(String.valueOf(allOrders.size()));
        lblActiveCarriers.setText(String.valueOf(carriers.size()));

        // BarChart Güncelle (Günlük Kazanç)
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Revenue");
        Map<String, Double> dailyRevenue = new TreeMap<>();
        for (Order o : allOrders) {
            if (o.isDelivered() && !o.isCancelled()) {
                String date = o.getOrderTime().toLocalDateTime().toLocalDate().toString();
                dailyRevenue.put(date, dailyRevenue.getOrDefault(date, 0.0) + o.getTotalCost());
            }
        }
        dailyRevenue.forEach((date, val) -> series.getData().add(new XYChart.Data<>(date, val)));
        salesChart.getData().add(series);

        // PieChart Güncelle (Sipariş Durumu)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Delivered (" + delivered + ")", delivered),
                new PieChart.Data("Pending (" + pending + ")", pending),
                new PieChart.Data("Cancelled (" + cancelled + ")", cancelled)
        );
        statusPieChart.setData(pieData);
    }

    // --- CARRIER ACTIONS ---
    @FXML
    private void handleEmployCarrier() {
        String username = txtCarrierUser.getText().trim();
        String password = txtCarrierPass.getText();
        String address = txtCarrierAddress.getText().trim();

        if (username.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Alertutil.showWarningMessage("All fields must be filled!");
            return;
        }
        if (username.length() < 4 || password.length() < 4) {
            Alertutil.showWarningMessage("Username and Password must be at least 4 characters!");
            return;
        }

        if (showConfirm("Employ Carrier", "Hire " + username + " as a new carrier?")) {
            User newCarrier = new User(0, username, password, "carrier", address);
            try {
                if (userDAO.addUser(newCarrier)) {
                    refreshCarrierTable();
                    updateReports();
                    clearCarrierFields();
                    Alertutil.showSuccessMessage("Carrier employed successfully.");
                }
            } catch (SQLException e) {
                Alertutil.showErrorMessage("Database error: Username might be taken.");
            }
        }
    }

    @FXML
    private void handleFireCarrier() {
        User selected = tableCarriers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alertutil.showWarningMessage("Please select a carrier to fire.");
            return;
        }
        if (showConfirm("Fire Carrier", "Terminate employment for " + selected.getUsername() + "?")) {
            if (userDAO.deleteUser(selected.getId())) {
                refreshCarrierTable();
                updateReports();
                Alertutil.showSuccessMessage("Carrier fired.");
            }
        }
    }

    // --- PRODUCT ACTIONS ---
    @FXML
    private void handleAddProduct() {
        if (!isProductInputValid()) {
            Alertutil.showWarningMessage("Please fill all product fields correctly.");
            return;
        }
        String name = txtProductName.getText().trim();
        double price = Double.parseDouble(txtProductPrice.getText());
        double stock = Double.parseDouble(txtProductStock.getText());
        double threshold = Double.parseDouble(txtProductThreshold.getText());

        if (showConfirm("Add Product", "Add " + name + " to system?")) {
            Product p = new Product(
                    0,
                    name,
                    comboProductType.getValue(),
                    price,
                    stock,
                    selectedImageBytes,
                    threshold
            );
            if (productDAO.addProduct(p)) {
                refreshProductTable();
                clearProductFields();
                Alertutil.showSuccessMessage("Product added.");
            }
        }
    }

    @FXML
    private void handleUpdateProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) { 
            Alertutil.showWarningMessage("Select product first."); 
            return; 
        }
        if (!isProductInputValid()) return;

        if (showConfirm("Update Product", "Save changes?")) {
            selected.setName(txtProductName.getText());
            selected.setPrice(Double.parseDouble(txtProductPrice.getText()));
            selected.setStock(Double.parseDouble(txtProductStock.getText()));
            selected.setThreshold(Double.parseDouble(txtProductThreshold.getText()));
            selected.setType(comboProductType.getValue());
            selected.setImage(selectedImageBytes);


            if (productDAO.updateProduct(selected)) {
                refreshProductTable();
                Alertutil.showSuccessMessage("Product updated.");
            }
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null && showConfirm("Delete", "Delete " + selected.getName() + "?")) {
            if (productDAO.deleteProduct(selected.getId())) {
                refreshProductTable();
                Alertutil.showSuccessMessage("Product deleted.");
            }
        }
    }

    @FXML
    private void handleLogout() {
        if (showConfirm("Logout", "Are you sure?")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/resources/Login.fxml"));
                Stage stage = (Stage) tableProducts.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
            } catch (IOException e) {
                Alertutil.showErrorMessage("Logout error: " + e.getMessage());
            }
        }
    }

    // --- HELPERS ---
    private void refreshProductTable() {
        tableProducts.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
    }
    private void refreshCarrierTable() {
        tableCarriers.setItems(FXCollections.observableArrayList(userDAO.getUsersByRole("carrier")));
    }
    private void refreshOrderTable() {
        tableOrders.setItems(FXCollections.observableArrayList(orderDAO.getAllOrdersWithDetails()));
    }

    private boolean isProductInputValid() {
        try {
            if (txtProductName.getText().isEmpty() || comboProductType.getValue() == null) return false;
            Double.parseDouble(txtProductPrice.getText());
            Double.parseDouble(txtProductStock.getText());
            Double.parseDouble(txtProductThreshold.getText());
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        a.setHeaderText(null);
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private void clearProductFields() {
        txtProductName.clear(); txtProductPrice.clear(); txtProductStock.clear(); txtProductThreshold.clear();
        comboProductType.getSelectionModel().clearSelection();
        productImageView.setImage(null);
        imageInfoLabel.setText("Drag & drop image or choose.");
        selectedImageBytes = null;
    }
    private void clearCarrierFields() {
        txtCarrierUser.clear(); txtCarrierPass.clear(); txtCarrierAddress.clear();
    }

    @FXML
    private void handleOpenMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/resources/MessageView.fxml")
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Customer Messages");

            User owner = getCurrentOwner();
            User customer = findFirstCustomer();

            ((MessageController) loader.getController())
                    .setUsers(owner, customer);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private User getCurrentOwner() throws Exception {
        String sql = "SELECT * FROM userinfo WHERE role='owner' LIMIT 1";

        try (var c = DatabaseConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        null,
                        rs.getString("role"),
                        rs.getString("address")
                );
            }
        }
        throw new RuntimeException("Owner not found");
    }

    private User findFirstCustomer() throws Exception {
        String sql = "SELECT * FROM userinfo WHERE role='customer' LIMIT 1";

        try (var c = DatabaseConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        null,
                        rs.getString("role"),
                        rs.getString("address")
                );
            }
        }
        throw new RuntimeException("Customer not found");
    }

    // kupon için
    /**
     * Load all coupons from the database and show them in the table.
     * This method is called when "Refresh" button is pressed.
     */
    @FXML
    private void handleRefreshCoupons() {
        tableCoupons.setItems(FXCollections.observableArrayList(couponDAO.getUserCoupons(0))); // 0 = show all
    }

    /**
     * Create a new coupon when the owner clicks the button.
     * It checks for valid inputs and allowed value ranges before sending data to the database.
     */
    @FXML
    private void handleCreateCoupon() {
        String code = txtCouponCode.getText().trim();
        String rateStr = txtCouponRate.getText().trim();
        String daysStr = txtCouponDays.getText().trim();
        String minStr = txtCouponMin.getText().trim();

        if (code.isEmpty() || rateStr.isEmpty() || daysStr.isEmpty() || minStr.isEmpty()) {
            Alertutil.showWarningMessage("All coupon fields must be filled!");
            return;
        }

        try {
            double rate = Double.parseDouble(rateStr);
            int days = Integer.parseInt(daysStr);
            double minValue = Double.parseDouble(minStr);

            // --- VALIDATION RULES ---
            if (rate <= 0 || rate > 50) {
                Alertutil.showWarningMessage("Discount rate must be between 1 and 50!");
                return;
            }
            if (days <= 0 || days > 365) {
                Alertutil.showWarningMessage("Valid days must be between 1 and 365!");
                return;
            }
            if (minValue < 0 || minValue > 10000) {
                Alertutil.showWarningMessage("Minimum cart value must be between 0 and 10000!");
                return;
            }

            if (showConfirm("Create Coupon", "Add new coupon: " + code + "?")) {
                boolean created = couponDAO.createCoupon(code, rate, days, minValue, null);
                if (created) {
                    handleRefreshCoupons();
                    clearCouponFields();
                    Alertutil.showSuccessMessage("Coupon created successfully.");
                } else {
                    Alertutil.showErrorMessage("Error creating coupon.");
                }
            }

        } catch (NumberFormatException e) {
            Alertutil.showWarningMessage("Invalid number format!");
        }
    }


    /**
     * Clear all text fields in the coupon form.
     */
    private void clearCouponFields() {
        txtCouponCode.clear();
        txtCouponRate.clear();
        txtCouponDays.clear();
        txtCouponMin.clear();
    }

    /**
     * Initialize coupon table columns when OwnerView is loaded.
     * This should be called from initialize().
     */
    private void setupCouponColumns() {
        colCouponId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCouponCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCouponRate.setCellValueFactory(new PropertyValueFactory<>("discountRate"));
        colCouponExpire.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        colCouponActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    }


    private void loadCustomers() {
        try {
            String sql = "SELECT * FROM userinfo WHERE role='customer'";
            var list = FXCollections.<User>observableArrayList();

            try (var c = DatabaseConnection.getConnection();
                 var ps = c.prepareStatement(sql);
                 var rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            null,
                            rs.getString("role"),
                            rs.getString("address")
                    ));
                }
            }

            customerBox.setItems(list);

            // ComboBox’ta username görünsün
            customerBox.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(User u, boolean empty) {
                    super.updateItem(u, empty);
                    setText(empty || u == null ? "" : u.getUsername());
                }
            });
            customerBox.setButtonCell(customerBox.getCellFactory().call(null));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isImageFile(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".png") || n.endsWith(".jpg")
                || n.endsWith(".jpeg") || n.endsWith(".gif");
    }

    private void loadImageFromFile(File file) {
        try {
            Image img = new Image(file.toURI().toString());
            productImageView.setImage(img);

            selectedImageBytes = Files.readAllBytes(file.toPath());
            imageInfoLabel.setText(file.getName() + " (" + selectedImageBytes.length / 1024 + " KB)");
        } catch (Exception e) {
            selectedImageBytes = null;
            imageInfoLabel.setText("Image load failed.");
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Product Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fc.showOpenDialog(tableProducts.getScene().getWindow());
        if (file != null) {
            loadImageFromFile(file);
        }
    }


}