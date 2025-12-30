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

        loadProducts();
    }

    private void loadProducts()
    {
        // Using the table name
        String sql = "SELECT * FROM productinfo";

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
}