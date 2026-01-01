package controller;

import database.DatabaseConnection;
import dao.OrderDAO;
import model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.Alertutil;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> nameColumn, typeColumn;
    @FXML private TableColumn<Product, Double> priceColumn, stockColumn;
    @FXML private TableColumn<Product, ImageView> imageColumn;
    @FXML private Label welcomeLabel;
    @FXML private TextField amountField;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<Integer> deliveryHourBox;
    @FXML private TextField searchField;

    private final Cart cart = new Cart();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct;
    private User currentUser;
    private static final double MIN_ORDER_AMOUNT = 200.0;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        imageColumn.setCellValueFactory(cellData -> {
            byte[] imageData = cellData.getValue().getImage();
            ImageView imageView = new ImageView();
            if (imageData != null) {
                imageView.setImage(new Image(new ByteArrayInputStream(imageData)));
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) selectedProduct = newVal;
        });

        for (int h = 0; h < 24; h++) deliveryHourBox.getItems().add(h);
        deliveryHourBox.getSelectionModel().select(LocalTime.now().getHour());

        loadProducts();

        // --- Search filter setup ---
        FilteredList<Product> filteredData = new FilteredList<>(productList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                // arama kutusu boşsa tüm ürünleri göster
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // isim veya ürün tipiyle eşleşirse
                if (product.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (product.getType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // eşleşmiyorsa gizle
            });
        });

        // Sıralama desteği (tablo başlıklarına tıklama)
        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);


    }

    private void loadProducts() {
        String sql = "SELECT * FROM productinfo WHERE stock > 0 ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            productList.clear();
            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"), rs.getString("name"), rs.getString("type"),
                        rs.getDouble("price"), rs.getDouble("stock"), rs.getBytes("image"),
                        rs.getDouble("threshold")
                ));
            }
            productTable.setItems(productList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToCart() {
        if (selectedProduct == null) {
            Alertutil.showWarningMessage("Please select a product first!");
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                Alertutil.showWarningMessage("Amount must be positive.");
                return;
            }
            if (amount > selectedProduct.getStock()) {
                Alertutil.showWarningMessage("Not enough stock.");
                return;
            }
            cart.addItem(selectedProduct, amount);
            Alertutil.showSuccessMessage(amount + " kg added to cart.");
            amountField.clear();
        } catch (NumberFormatException e) {
            Alertutil.showErrorMessage("Please enter a valid numeric amount.");
        }
    }

    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/CartView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Shopping Cart");
            stage.setScene(new Scene(loader.load()));
            CartController controller = loader.getController();
            controller.setCart(cart);
            stage.show();
        } catch (IOException e) {
            Alertutil.showErrorMessage("Failed to open cart.");
        }
    }

    @FXML
    private void handleCompletePurchase() {
        if (cart.getItems().isEmpty()) {
            Alertutil.showWarningMessage("Your cart is empty.");
            return;
        }
        if (cart.getTotalPrice() < MIN_ORDER_AMOUNT) {
            Alertutil.showWarningMessage("Minimum order amount is " + MIN_ORDER_AMOUNT + " TL");
            return;
        }
        if (deliveryDatePicker.getValue() == null) {
            Alertutil.showWarningMessage("Please select delivery date.");
            return;
        }

        LocalDateTime deliveryTime = LocalDateTime.of(deliveryDatePicker.getValue(), LocalTime.of(deliveryHourBox.getValue(), 0));
        LocalDateTime now = LocalDateTime.now();

        if (deliveryTime.isBefore(now) || deliveryTime.isAfter(now.plusHours(48))) {
            Alertutil.showWarningMessage("Delivery must be within 48 hours from now.");
            return;
        }

        if (new OrderDAO().placeOrderWithTransaction(currentUser, cart, deliveryTime)) {
            cart.clear();
            loadProducts();
            Alertutil.showSuccessMessage("Order placed successfully!");
            deliveryDatePicker.setValue(null);
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Parent loginRoot = FXMLLoader.load(getClass().getResource("/resources/Login.fxml"));
                Stage currentStage = (Stage) productTable.getScene().getWindow();
                currentStage.setScene(new Scene(loginRoot));
                currentStage.centerOnScreen();
            } catch (IOException e) {
                Alertutil.showErrorMessage("Logout failed: " + e.getMessage());
            }
        }
    }
}