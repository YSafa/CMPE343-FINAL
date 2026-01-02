package controller;

import database.DatabaseConnection;
import dao.OrderDAO;
import model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Alertutil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML private FlowPane productsFlowPane;
    @FXML private Label welcomeLabel;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<Integer> deliveryHourBox;
    @FXML private TextField searchField;
    @FXML private Button btnAllProducts, btnVegetables, btnFruits, btnComplete;

    private final Cart cart = new Cart();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private FilteredList<Product> filteredData;
    private User currentUser;
    private String activeCategory = "All";
    private static final double MIN_ORDER_AMOUNT = 200.0;

    private final String activeStyle = "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;";
    private final String inactiveStyle = "-fx-background-color: white; -fx-border-color: #2e7d32; -fx-border-radius: 10; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String pressedStyle = "-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDeliveryUI();
        loadProducts();
        setupSearchFilter();
        updateCategoryStyles(btnAllProducts);
        
        addClickEffect(btnAllProducts);
        addClickEffect(btnVegetables);
        addClickEffect(btnFruits);
    }

    private void setupDeliveryUI() {
        for (int h = 0; h < 24; h++) deliveryHourBox.getItems().add(h);
        deliveryHourBox.getSelectionModel().select(LocalTime.now().getHour());
        
        btnComplete.setStyle(activeStyle);
        btnComplete.setOnMousePressed(e -> {
            btnComplete.setStyle(pressedStyle);
            btnComplete.setScaleX(0.95);
            btnComplete.setScaleY(0.95);
        });
        btnComplete.setOnMouseReleased(e -> {
            btnComplete.setStyle(activeStyle);
            btnComplete.setScaleX(1.0);
            btnComplete.setScaleY(1.0);
        });
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(productList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
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
            renderProductCards(productList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredData.setPredicate(product -> {
            boolean matchesSearch = product.getName().toLowerCase().contains(searchText) || 
                                    product.getType().toLowerCase().contains(searchText);
            boolean matchesCategory = activeCategory.equals("All") || 
                                      product.getType().equalsIgnoreCase(activeCategory);
            return matchesSearch && matchesCategory;
        });
        renderProductCards(filteredData);
    }

    @FXML private void handleFilterAll() { activeCategory = "All"; updateCategoryStyles(btnAllProducts); applyFilters(); }
    @FXML private void handleFilterVegetables() { activeCategory = "vegetable"; updateCategoryStyles(btnVegetables); applyFilters(); }
    @FXML private void handleFilterFruits() { activeCategory = "fruit"; updateCategoryStyles(btnFruits); applyFilters(); }

    private void updateCategoryStyles(Button selected) {
        btnAllProducts.setStyle(inactiveStyle);
        btnVegetables.setStyle(inactiveStyle);
        btnFruits.setStyle(inactiveStyle);
        selected.setStyle(activeStyle);
    }

    private void addClickEffect(Button btn) {
        btn.setOnMousePressed(e -> {
            btn.setScaleX(0.9);
            btn.setScaleY(0.9);
        });
        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
    }

    private void renderProductCards(List<Product> products) {
        productsFlowPane.getChildren().clear();
        for (Product product : products) {
            VBox card = new VBox(10);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            card.setPrefWidth(220);

            ImageView iv = new ImageView();
            if (product.getImage() != null) iv.setImage(new Image(new ByteArrayInputStream(product.getImage())));
            iv.setFitWidth(120); iv.setFitHeight(120); iv.setPreserveRatio(true);

            Label name = new Label(product.getName()); name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            Label price = new Label(String.format("%.2f ₺/kg", product.getPrice())); price.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            Label stock = new Label("Stock: " + product.getStock() + " kg"); stock.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

            TextField qty = new TextField(); qty.setPromptText("kg"); qty.setPrefWidth(60); qty.setStyle("-fx-background-radius: 10;");

            Button addBtn = new Button("ADD");
            String normalGreen = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;";
            String pressedGreen = "-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";
            
            addBtn.setStyle(normalGreen);
            addBtn.setOnMousePressed(e -> {
                addBtn.setStyle(pressedGreen);
                addBtn.setScaleX(0.9);
                addBtn.setScaleY(0.9);
            });
            addBtn.setOnMouseReleased(e -> {
                addBtn.setStyle(normalGreen);
                addBtn.setScaleX(1.0);
                addBtn.setScaleY(1.0);
            });
            addBtn.setOnAction(e -> processAddToCart(product, qty));

            HBox actions = new HBox(10, qty, addBtn); actions.setAlignment(Pos.CENTER);
            card.getChildren().addAll(iv, name, price, stock, actions);
            productsFlowPane.getChildren().add(card);
        }
    }

    private void processAddToCart(Product p, TextField q) {
        try {
            double amount = Double.parseDouble(q.getText());
            if (amount <= 0 || amount > p.getStock()) { 
                Alertutil.showWarningMessage("Invalid amount or stock."); 
                return; 
            }
            cart.addItem(p, amount);
            Alertutil.showSuccessMessage(amount + " kg " + p.getName() + " added to cart.");
            q.clear();
        } catch (Exception e) { 
            Alertutil.showErrorMessage("Enter numeric amount."); 
        }
    }

    @FXML private void handleViewCart() { openWindow("/resources/CartView.fxml", "Cart"); }
    @FXML private void handleViewOrders() { openWindow("/resources/CustomerOrdersView.fxml", "My Orders"); }
    @FXML private void handleRefresh() { loadProducts(); searchField.clear(); handleFilterAll(); }

    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
            Stage s = new Stage(); s.setTitle(title); s.setScene(new Scene(l.load()));
            if (fxml.contains("Cart")) ((CartController) l.getController()).setCart(cart);
            if (fxml.contains("Orders")) ((CustomerOrdersController) l.getController()).setCurrentUser(currentUser);
            s.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleCompletePurchase() {
        if (cart.getItems().isEmpty() || cart.getTotalPrice() < MIN_ORDER_AMOUNT || deliveryDatePicker.getValue() == null) {
            Alertutil.showWarningMessage("Check cart amount (min 200TL) and delivery date.");
            return;
        }

        LocalDateTime dt = LocalDateTime.of(
                deliveryDatePicker.getValue(),
                LocalTime.of(deliveryHourBox.getValue(), 0)
        );

        // Kupon bilgisi alınıyor
        Coupon usedCoupon = cart.getAppliedCoupon();
        String usedCouponCode = (usedCoupon != null) ? usedCoupon.getCode() : null;

        // Siparişi ver
        boolean success = new OrderDAO().placeOrderWithTransaction(currentUser, cart, dt, usedCouponCode);

        if (success) {
            // Başarılıysa kullanıcıya detaylı bilgi ver
            StringBuilder message = new StringBuilder("Order placed successfully!");

            if (usedCoupon != null) {
                message.append("\n\n Coupon '")
                        .append(usedCoupon.getCode())
                        .append("' applied (")
                        .append(usedCoupon.getDiscountRate())
                        .append("% discount).");
            }

            Alertutil.showSuccessMessage(message.toString());

            cart.clear(); // sepeti temizle
            loadProducts(); // ürünleri yenile
        } else {
            Alertutil.showErrorMessage("Failed to complete the order. Please try again.");
        }
    }



    @FXML private void handleLogout() {
        if (Alertutil.showConfirmation("Logout", "Are you sure you want to log out?")) {
            try {
                Parent r = FXMLLoader.load(getClass().getResource("/resources/Login.fxml"));
                Stage s = (Stage) productsFlowPane.getScene().getWindow();
                s.setScene(new Scene(r)); s.centerOnScreen();
            } catch (IOException e) { 
                e.printStackTrace(); 
            }
        }
    }

    @FXML
    private void handleMessageOwner() {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/resources/MessageView.fxml"));
            Stage s = new Stage();
            s.setScene(new Scene(l.load()));

            User owner = findOwner();
            ((MessageController) l.getController()).setUsers(currentUser, owner);

            s.setTitle("Message Owner");
            s.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User findOwner() throws Exception {
        String sql = "SELECT * FROM userinfo WHERE role='owner' LIMIT 1";
        try (var c = DatabaseConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            if (rs.next())
                return new User(rs.getInt("id"), rs.getString("username"),
                        null, rs.getString("role"), rs.getString("address"));
        }
        throw new RuntimeException("Owner not found");
    }

}