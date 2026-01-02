package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserDAO;
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
import model.Order;
import model.Product;
import model.User;
import util.Alertutil;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;

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

    // --- DAOs ---
    private ProductDAO productDAO = new ProductDAO();
    private UserDAO userDAO = new UserDAO();
    private OrderDAO orderDAO = new OrderDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAllData();
        if (comboProductType != null) {
            comboProductType.setItems(FXCollections.observableArrayList("vegetable", "fruit"));
        }
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
            Product p = new Product(0, name, comboProductType.getValue(), price, stock, null, threshold);
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
    }
    private void clearCarrierFields() {
        txtCarrierUser.clear(); txtCarrierPass.clear(); txtCarrierAddress.clear();
    }
}