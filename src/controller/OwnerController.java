package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Order;
import model.Product;
import model.User;
import java.sql.Timestamp;
import java.util.List;

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
        comboProductType.setItems(FXCollections.observableArrayList("vegetable", "fruit")); 
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

    @FXML
    private void handleAddProduct() {
        try {
            Product p = new Product(0, 
                txtProductName.getText(), 
                comboProductType.getValue(), 
                Double.parseDouble(txtProductPrice.getText()), 
                Double.parseDouble(txtProductStock.getText()), 
                null, 
                Double.parseDouble(txtProductThreshold.getText()));
            
            if(productDAO.addProduct(p)) {
                refreshProductTable();
                clearProductFields();
            }
        } catch (Exception e) {
            showError("Input Error! Please check numeric values for Price, Stock, and Threshold.");
        }
    }

    @FXML
    private void handleUpdateProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setStock(Double.parseDouble(txtProductStock.getText()));
                productDAO.updateProduct(selected);
                refreshProductTable();
                updateSalesChart();
            } catch (Exception e) {
                showError("Update failed. Ensure numeric fields are correct.");
            }
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            productDAO.deleteProduct(selected.getId());
            refreshProductTable();
        }
    }

    @FXML
    private void handleFireCarrier() {
        User selected = tableCarriers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userDAO.deleteUser(selected.getId());
            refreshCarrierTable();
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
        for(Order o : orders) totalRevenue += o.getTotalCost();
        
        series.getData().add(new XYChart.Data<>("Total Revenue (TL)", totalRevenue));
        salesChart.getData().add(series);
    }

    private void clearProductFields() {
        txtProductName.clear();
        txtProductPrice.clear();
        txtProductStock.clear();
        txtProductThreshold.clear();
    }

    private void showError(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error"); 
    alert.setHeaderText(null);
    alert.setContentText(msg);
    alert.showAndWait(); 
}
}