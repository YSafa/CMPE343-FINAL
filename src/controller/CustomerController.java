package controller;

import database.DatabaseConnection;
import model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import util.Alertutil;
import model.Cart;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;



/**
 * Controller for the Customer view.
 * This class allows customers to browse products, select them,
 * and add desired quantities to the shopping cart.
 */
public class CustomerController implements Initializable
{

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> typeColumn; // For 'fruit' or 'vegetable'

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Double> stockColumn; // changed to Double

    @FXML
    private TableColumn<Product, ImageView> imageColumn;

    @FXML
    private javafx.scene.control.TextField amountField; // Miktar girişi için

    private Product selectedProduct; // Seçilen ürünü takip etmek için
    //private model.Cart cart = new model.Cart(); // Müşterinin sepetini oluştur
    private final Cart cart = new Cart();

    // List to hold data
    private final ObservableList<Product> productList = FXCollections.observableArrayList();


    /**
     * Initializes the table columns and loads all products from the database.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Bind columns to Product class fields
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));


        // Add an ImageView column to show product images
        imageColumn.setCellValueFactory(cellData ->
        { byte[] imageData = cellData.getValue().getImage();
            if (imageData != null)
            { ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(imageData)));
                imageView.setFitWidth(60); imageView.setFitHeight(60); imageView.setPreserveRatio(true);
                return new javafx.beans.property.SimpleObjectProperty<>(imageView);
            } else
            {
                return new javafx.beans.property.SimpleObjectProperty<>(new ImageView());
            }
        });


        // TABLO SEÇİM DİNLEYİCİSİ: Satıra tıklandığında ürünü 'selectedProduct' içine atar
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
            }
        });

        loadProducts();
    }

    /**
     * Loads products from the database and populates the TableView.
     */
    private void loadProducts()
    {
        // Using the table name
        String sql = "SELECT * FROM productinfo WHERE stock  > 0 ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            productList.clear(); // Clear list before loading

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"), // Enum comes as String
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getBytes("image"),
                        rs.getDouble("threshold")
                );
                productList.add(product);
            }

            productTable.setItems(productList);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading products from productinfo.");
        }
    }


    /**
     * Adds the selected product to the cart.
     */
    @FXML
    private void handleAddToCart() {
        // 1. Seçim kontrolü
        if (selectedProduct == null) {
            Alertutil.showWarningMessage("Please select a product from the table first!");
            return;
        }

        try {
            // 2. Boş miktar kontrolü
            String text = amountField.getText();
            if (text == null || text.isEmpty()) {
                Alertutil.showWarningMessage("Please enter an amount.");
                return;
            }

            double amount = Double.parseDouble(text);

            // 3. Negatif/Sıfır miktar kontrolü (Logical Error)
            if (amount <= 0) {
                Alertutil.showWarningMessage("Amount must be greater than zero.");
                return;
            }

            // 4. Stok yeterli mi kontrolü
            if (amount > selectedProduct.getStock()) {
                Alertutil.showWarningMessage("Not enough stock! Available: " + selectedProduct.getStock() + " kg");
                return;
            }

            // 5. Sepete ekle ve giriş alanını temizle
            cart.addItem(selectedProduct, amount);
            Alertutil.showSuccessMessage(String.format("%.2f kg of %s added to cart.", amount, selectedProduct.getName()));
            amountField.clear();

        } catch (NumberFormatException e) {
            // Harf girilirse projede belirtilen hatayı engellemiş oluyoruz
            Alertutil.showErrorMessage("Error: Please enter a valid numeric value (e.g. 1.5)");
        }
    }

    /**
     * Handles the "View Cart" button action.
     * Displays the user's shopping cart in a future implementation.
     */
    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/resources/CartView.fxml"));

            Scene scene = new Scene(loader.load());

            CartController controller = loader.getController();
            controller.setCart(cart);

            Stage stage = new Stage();
            stage.setTitle("Shopping Cart");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alertutil.showErrorMessage("Failed to open cart view.");
        }
    }



    /**
     * Handles the "Logout" button action.
     * Logs the user out and redirects back to the login screen.
     */
    @FXML
    private void handleLogout() {
        Alertutil.showInfoMessage("You have been logged out.");
    }

    /**
     * Handles the "Complete Purchase" button action.
     * Completes the current order and clears the shopping cart.
     */
    @FXML
    private void handleCompletePurchase() {
        Alertutil.showSuccessMessage("Purchase completed successfully!");
    }






}