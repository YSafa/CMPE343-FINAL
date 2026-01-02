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

/**
 * This controller manages the customer main screen.
 * It loads products, shows them as cards, and lets the user add items to the cart.
 * It also supports search, category filter, viewing cart/orders, and completing purchase.
 */
public class CustomerController implements Initializable {


    /** FlowPane that holds product cards. */
    @FXML private FlowPane productsFlowPane;

    /** Label that shows welcome text for the user. */
    @FXML private Label welcomeLabel;

    /** Date picker for delivery date selection. */
    @FXML private DatePicker deliveryDatePicker;

    /** ComboBox for selecting delivery hour (0-23). */
    @FXML private ComboBox<Integer> deliveryHourBox;

    /** Text field used to search products by name or type. */
    @FXML private TextField searchField;

    /** Buttons for category filters and completing purchase. */
    @FXML private Button btnAllProducts, btnVegetables, btnFruits, btnComplete;

    /** Cart of the current customer session. */
    private final Cart cart = new Cart();

    /** List of all available products from database. */
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    /** Filtered view of product list (search + category). */
    private FilteredList<Product> filteredData;

    /** Current logged-in user. */
    private User currentUser;

    /** Active category filter name. */
    private String activeCategory = "All";

    /** Minimum order amount required to place an order. */
    private static final double MIN_ORDER_AMOUNT = 200.0;

    /** Style used for selected category button. */
    private final String activeStyle =
            "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;";

    /** Style used for non-selected category buttons. */
    private final String inactiveStyle =
            "-fx-background-color: white; -fx-border-color: #2e7d32; -fx-border-radius: 10; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-cursor: hand;";

    /** Style used when a button is pressed. */
    private final String pressedStyle =
            "-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;";


    /**
     * Sets the current user for this screen.
     * It also updates the welcome label.
     *
     * @param user the logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
    }

    /**
     * Initializes the screen when FXML is loaded.
     * It prepares delivery UI, loads products, and starts search filtering.
     *
     * @param location URL location info (from JavaFX)
     * @param resources resource bundle (from JavaFX)
     */
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

    /**
     * Sets delivery hour options and button styles for completing purchase.
     * It fills the hour box with 0-23 and selects current hour.
     */
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

    /**
     * Creates a filtered list for products.
     * It listens to search text changes and applies filters.
     */
    private void setupSearchFilter() {
        filteredData = new FilteredList<>(productList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }


    /**
     * Loads products from the database.
     * It only loads products with stock > 0 and orders by name.
     * After loading, it renders product cards.
     */
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


    /**
     * Applies search and category filters on the product list.
     * It filters by product name/type and by active category.
     */
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

    /**
     * Shows all products (removes category filter).
     */
    @FXML private void handleFilterAll() {
        activeCategory = "All";
        updateCategoryStyles(btnAllProducts);
        applyFilters();
    }

    /**
     * Shows only vegetable products.
     */
    @FXML private void handleFilterVegetables() {
        activeCategory = "vegetable";
        updateCategoryStyles(btnVegetables);
        applyFilters();
    }

    /**
     * Shows only fruit products.
     */
    @FXML private void handleFilterFruits() {
        activeCategory = "fruit";
        updateCategoryStyles(btnFruits);
        applyFilters();
    }

    /**
     * Updates button styles for category selection.
     *
     * @param selected the selected category button
     */
    private void updateCategoryStyles(Button selected) {
        btnAllProducts.setStyle(inactiveStyle);
        btnVegetables.setStyle(inactiveStyle);
        btnFruits.setStyle(inactiveStyle);
        selected.setStyle(activeStyle);
    }

    /**
     * Adds a small press/release scale effect to a button.
     *
     * @param btn the button to style
     */
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

    /**
     * Creates and shows product cards in the FlowPane.
     * Each card has image, name, price, stock, and an "ADD" button.
     *
     * @param products list of products to render
     */
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


    /**
     * Adds a product to the cart with the given quantity.
     * It checks quantity is numeric, positive, and not more than stock.
     *
     * @param p the product to add
     * @param q the text field that contains the quantity
     */
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

    /**
     * Opens the cart window.
     */
    @FXML private void handleViewCart() {
        openWindow("/resources/CartView.fxml", "Cart");
    }

    /**
     * Opens the customer orders window.
     */
    @FXML private void handleViewOrders() {
        openWindow("/resources/CustomerOrdersView.fxml", "My Orders");
    }

    /**
     * Reloads products and resets search and filter.
     */
    @FXML private void handleRefresh() {
        loadProducts();
        searchField.clear();
        handleFilterAll();
    }


    /**
     * Opens a new window by loading an FXML file.
     * If it is cart or orders window, it also sets needed data to controller.
     *
     * @param fxml  path of the FXML file
     * @param title window title
     */
    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
            Stage s = new Stage(); s.setTitle(title); s.setScene(new Scene(l.load()));
            if (fxml.contains("Cart")) ((CartController) l.getController()).setCart(cart);
            if (fxml.contains("Orders")) ((CustomerOrdersController) l.getController()).setCurrentUser(currentUser);
            s.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Completes the purchase and places an order.
     * It checks:
     * - cart is not empty
     * - total price is at least 200 TL
     * - delivery date is selected
     * Then it creates a delivery time and sends order to database.
     */
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


    /**
     * Logs out the current user and returns to login screen.
     * It asks for confirmation before logout.
     */
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


    /**
     * Opens the message window to send a message to the owner.
     * It finds the owner user from database and sets both users in controller.
     */
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

    /**
     * Finds the owner user from the database.
     * It searches userinfo table with role 'owner'.
     *
     * @return the owner user
     * @throws Exception if database error happens
     */
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