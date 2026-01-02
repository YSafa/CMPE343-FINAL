package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Cart;
import model.CartItem;
//kupon için
import dao.CouponDAO;
import model.Coupon;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * This controller manages the cart screen.
 * It shows cart items in a table.
 * It also calculates subtotal, VAT, total, and coupon discount.
 */

public class CartController {

    /** Table that shows cart items. */
    @FXML
    private TableView<CartItem> cartTable;

    /** Column for product name. */
    @FXML
    private TableColumn<CartItem, String> nameColumn;

    /** Column for product quantity. */
    @FXML
    private TableColumn<CartItem, Double> quantityColumn;

    /** Column for unit price of the product. */
    @FXML
    private TableColumn<CartItem, Double> unitPriceColumn;

    /** Column for total price of one cart row (quantity * price). */
    @FXML
    private TableColumn<CartItem, Double> totalPriceColumn;

    /** Label that shows the final total price. */
    @FXML
    private Label totalLabel;

    /** Label that shows subtotal (without VAT). */
    @FXML
    private Label subtotalLabel;

    /** Label that shows VAT amount. */
    @FXML
    private Label vatLabel;

    /** Text field where user writes coupon code. */
    @FXML private TextField couponField;

    /** Label that shows coupon status messages. */
    @FXML private Label couponMessage;

    /** Cart object that holds selected items. */
    private Cart cart;

    /** The coupon that is currently applied to the cart (can be null). */
    private Coupon appliedCoupon;

    /** Observable list used by TableView to show cart items. */
    private final ObservableList<CartItem> cartItems =
            FXCollections.observableArrayList();

    /**
     * Sets the cart for this controller.
     * It loads cart items into the table and updates totals.
     *
     * @param cart the cart to show in the UI
     */
    public void setCart(Cart cart) {
        this.cart = cart;

        cartItems.setAll(cart.getItems());
        cartTable.setItems(cartItems);
        updateTotals();

    }

    /**
     * Initializes the table columns.
     * This method runs automatically when the FXML is loaded.
     */
    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getProduct().getName()
                ));

        quantityColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getQuantity()
                ));

        unitPriceColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getProduct().getPrice()
                ));

        totalPriceColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getTotalItemPrice()
                ));
        cartTable.setItems(cartItems);
    }

    /**
     * Refreshes the table items and totals.
     * If a coupon is applied but cart is below minimum value,
     * it removes the coupon.
     */
    private void refresh() {
        if (cart == null) return;
        cartItems.setAll(cart.getItems());

        // Eğer kupon varsa ama sepet artık min. tutarı karşılamıyorsa sıfırla
        if (appliedCoupon != null) {
            double subtotal = cartItems.stream().mapToDouble(CartItem::getTotalItemPrice).sum();
            if (subtotal < appliedCoupon.getMinCartValue()) {
                couponMessage.setText("Coupon removed (cart below ₺" + appliedCoupon.getMinCartValue() + ")");
                couponMessage.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
            }
        }

        updateTotals();
    }


    /**
     * Called when user clicks "Apply" button for coupon.
     * It checks if coupon exists, is active, and fits the conditions.
     */
    @FXML
    private void handleApplyCoupon() {
        String code = couponField.getText().trim();

        if (code.isEmpty()) {
            showError("Please enter a coupon code.");
            return;
        }

        CouponDAO couponDAO = new CouponDAO();
        Coupon coupon = couponDAO.getCouponByCode(code);

        if (coupon == null) {
            couponMessage.setText("Invalid or inactive coupon!");
            couponMessage.setStyle("-fx-text-fill: red;");
            appliedCoupon = null;
            updateTotals();
            return;
        }

        // check expiration
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (coupon.getExpirationDate() != null && coupon.getExpirationDate().before(now)) {
            couponMessage.setText("Coupon expired!");
            couponMessage.setStyle("-fx-text-fill: red;");
            appliedCoupon = null;
            updateTotals();
            return;
        }

        // check min cart value
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotalItemPrice).sum();
        if (subtotal < coupon.getMinCartValue()) {
            couponMessage.setText("Min cart value ₺" + coupon.getMinCartValue());
            couponMessage.setStyle("-fx-text-fill: red;");
            appliedCoupon = null;
            updateTotals();
            return;
        }

        // valid coupon
        appliedCoupon = coupon;
        couponMessage.setText("Coupon applied: -" + coupon.getDiscountRate() + "%");
        couponMessage.setStyle("-fx-text-fill: green;");
        updateTotals();
    }


    /**
     * Closes the cart window.
     * If a coupon is applied, it clears coupon fields before closing.
     */
    @FXML
    private void handleClose() {
        if (appliedCoupon != null) {
            couponField.clear();
            couponMessage.setText("");
            appliedCoupon = null;
        }
        Stage stage = (Stage) cartTable.getScene().getWindow();
        stage.close();
    }


    /**
     * Removes the selected item from the cart.
     * If no item is selected, it shows an error message.
     */
    @FXML
    private void handleRemove() {

        if (cart == null) {
            showError("Cart is not set (cart == null). setCart() çağrılıyor mu kontrol et.");
            return;
        }

        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an item to remove.");
            return;
        }

        cart.removeItem(selected.getProduct());
        refresh();
        updateTotals();
    }

    /**
     * Shows an error alert with a message.
     *
     * @param msg the error message to show
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private static final double VAT_RATE = 0.18;

    /**
     * Updates the subtotal, VAT, and total with possible coupon discount.
     */
    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotalItemPrice).sum();
        double vatAmount = subtotal * VAT_RATE;
        double total = subtotal + vatAmount;
        double discount = 0.0;

        if (appliedCoupon != null) {
            discount = total * (appliedCoupon.getDiscountRate() / 100);
            total -= discount;
        }

        subtotalLabel.setText(String.format("Subtotal: ₺%.2f", subtotal));
        vatLabel.setText(String.format("VAT (18%%): ₺%.2f", vatAmount));

        if (appliedCoupon != null) {
            totalLabel.setText(String.format("Total: ₺%.2f  (Discount: -₺%.2f)", total, discount));
        } else {
            totalLabel.setText(String.format("Total (incl. VAT): ₺%.2f", total));
        }
    }




}
