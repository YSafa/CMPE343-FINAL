package controller;

import database.DatabaseConnection;
import model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import util.Alertutil;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

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
    private javafx.scene.control.TextField amountField; // Miktar girişi için

    private Product selectedProduct; // Seçilen ürünü takip etmek için
    private model.Cart cart = new model.Cart(); // Müşterinin sepetini oluştur

    // List to hold data
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Bind columns to Product class fields
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // TABLO SEÇİM DİNLEYİCİSİ: Satıra tıklandığında ürünü 'selectedProduct' içine atar
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
            }
        });

        loadProducts();
    }

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
    @FXML
    private void handleAddToCart() {
        // 1. Seçim kontrolü
        if (selectedProduct == null) {
            Alertutil.showWarningMessage("Lütfen önce tablodan bir ürün seçin!");
            return;
        }

        try {
            // 2. Boş miktar kontrolü
            String text = amountField.getText();
            if (text == null || text.isEmpty()) {
                Alertutil.showWarningMessage("Lütfen bir miktar giriniz.");
                return;
            }

            double amount = Double.parseDouble(text);

            // 3. Negatif/Sıfır miktar kontrolü (Logical Error)
            if (amount <= 0) {
                Alertutil.showWarningMessage("Miktar 0'dan büyük olmalıdır!");
                return;
            }

            // 4. Stok yeterli mi kontrolü
            if (amount > selectedProduct.getStock()) {
                Alertutil.showWarningMessage("Yetersiz stok! Mevcut: " + selectedProduct.getStock() + " kg");
                return;
            }

            // 5. Sepete ekle ve giriş alanını temizle
            cart.addItem(selectedProduct, amount);
            Alertutil.showSuccessMessage(amount + " kg " + selectedProduct.getName() + " sepete eklendi.");
            amountField.clear();

        } catch (NumberFormatException e) {
            // Harf girilirse projede belirtilen hatayı engellemiş oluyoruz
            Alertutil.showErrorMessage("Hata: Lütfen geçerli bir sayı giriniz (Örn: 1.5)");
        }
    }

}