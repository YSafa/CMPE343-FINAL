package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Order;
import model.Product;
import model.User;
import util.Alertutil;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class OwnerController {

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice;
    @FXML private TableColumn<Product, Double> colProdStock;
    @FXML private TableColumn<Product, Double> colProdThreshold;
    
    @FXML private TextField txtProductName, txtProductPrice, txtProductStock, txtProductThreshold;
    @FXML private ComboBox<String> comboProductType;

    @FXML private TableView<User> tableCarriers;
    @FXML private TableColumn<User, Integer> colCarrId;
    @FXML private TableColumn<User, String> colCarrUser;
    @FXML private TableColumn<User, String> colCarrAddress;

    @FXML private TableView<Order> tableOrders;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, Timestamp> colOrderTime;
    @FXML private TableColumn<Order, Double> colOrderTotal;

    @FXML private BarChart<String, Number> salesChart;

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
        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProdStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProdThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));

        colCarrId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCarrUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCarrAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
    }

    private void loadAllData() {
        refreshProductTable();
        refreshCarrierTable();
        refreshOrderTable();
        updateSalesChart();
    }

    private String formatProductName(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String trimmed = name.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    private boolean isInputValid() {
        String name = txtProductName.getText();
        String priceText = txtProductPrice.getText();
        String stockText = txtProductStock.getText();
        String thresholdText = txtProductThreshold.getText();

        if (name == null || name.trim().isEmpty() || priceText.isEmpty() || stockText.isEmpty() || thresholdText.isEmpty()) {
            Alertutil.showWarningMessage("All fields must be filled!");
            return false;
        }

        if (!name.trim().matches("^[a-zA-ZÇŞĞÜÖİçşğüöı\\s]+$")) {
            Alertutil.showWarningMessage("Product name must contain only letters!");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);
            double stock = Double.parseDouble(stockText);
            double threshold = Double.parseDouble(thresholdText);

            if (price <= 0) {
                Alertutil.showWarningMessage("Price must be greater than zero!");
                return false;
            }
            if (stock < 0) {
                Alertutil.showWarningMessage("Stock cannot be negative!");
                return false;
            }
            if (threshold <= 0) {
                Alertutil.showWarningMessage("Threshold must be greater than zero!");
                return false;
            }
            
            if (comboProductType.getValue() == null) {
                Alertutil.showWarningMessage("Please select a product type!");
                return false;
            }

        } catch (NumberFormatException e) {
            Alertutil.showErrorMessage("Price, Stock and Threshold must be valid numbers!");
            return false;
        }

        return true;
    }

    @FXML
    private void handleAddProduct() {
        if (!isInputValid()) return;

        String formattedName = formatProductName(txtProductName.getText());
        double price = Double.parseDouble(txtProductPrice.getText());
        double stock = Double.parseDouble(txtProductStock.getText());
        double threshold = Double.parseDouble(txtProductThreshold.getText());

        if (showConfirm("Add Product", "Add " + formattedName + " to system?")) {
            Product p = new Product(0, formattedName, comboProductType.getValue(), price, stock, null, threshold);
            if (productDAO.addProduct(p)) {
                refreshProductTable();
                clearProductFields();
                Alertutil.showSuccessMessage("Product added successfully.");
            }
        }
    }

    @FXML
    private void handleUpdateProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alertutil.showWarningMessage("Please select a product to update.");
            return;
        }

        if (!isInputValid()) return;

        String formattedName = formatProductName(txtProductName.getText());
        String newType = comboProductType.getValue();
        double newPrice = Double.parseDouble(txtProductPrice.getText());
        double newStock = Double.parseDouble(txtProductStock.getText());
        double newThreshold = Double.parseDouble(txtProductThreshold.getText());

        if (showConfirm("Update Product", "Save all changes for " + selected.getName() + "?")) {
            selected.setName(formattedName);
            selected.setType(newType);
            selected.setPrice(newPrice);
            selected.setStock(newStock);
            selected.setThreshold(newThreshold);

            if (productDAO.updateProduct(selected)) {
                refreshProductTable();
                updateSalesChart();
                Alertutil.showSuccessMessage("Product updated successfully.");
            }
        }
    }

    private boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleLogout() {
        if (showConfirm("Logout Confirmation", "Are you sure you want to log out?")) {
            try {
                Parent loginRoot = FXMLLoader.load(getClass().getResource("/resources/Login.fxml"));
                Stage currentStage = (Stage) tableProducts.getScene().getWindow();
                currentStage.setScene(new Scene(loginRoot));
                currentStage.centerOnScreen();
            } catch (IOException e) {
                Alertutil.showErrorMessage("Logout failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null && showConfirm("Delete Product", "Delete " + selected.getName() + " permanently?")) {
            if (productDAO.deleteProduct(selected.getId())) {
                refreshProductTable();
                updateSalesChart();
                Alertutil.showSuccessMessage("Product deleted.");
            }
        }
    }

    @FXML
    private void handleFireCarrier() {
        User selected = tableCarriers.getSelectionModel().getSelectedItem();
        if (selected != null && showConfirm("Fire Carrier", "Terminate employment for " + selected.getUsername() + "?")) {
            if (userDAO.deleteUser(selected.getId())) {
                refreshCarrierTable();
                Alertutil.showSuccessMessage("Carrier fired.");
            }
        }
    }

    private void refreshProductTable() {
        tableProducts.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
    }

    private void refreshCarrierTable() {
        tableCarriers.setItems(FXCollections.observableArrayList(userDAO.getUsersByRole("carrier")));
    }

    private void refreshOrderTable() {
        tableOrders.setItems(FXCollections.observableArrayList(orderDAO.getAllOrders()));
    }

    private void updateSalesChart() {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue Analysis");
        List<Order> orders = orderDAO.getAllOrders();
        double totalRevenue = 0;
        for (Order o : orders) totalRevenue += o.getTotalCost();
        series.getData().add(new XYChart.Data<>("Total Revenue", totalRevenue));
        salesChart.getData().add(series);
    }

    private void clearProductFields() {
        txtProductName.clear();
        txtProductPrice.clear();
        txtProductStock.clear();
        txtProductThreshold.clear();
        comboProductType.getSelectionModel().clearSelection();
    }
}