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


public class CartController {

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> nameColumn;

    @FXML
    private TableColumn<CartItem, Double> quantityColumn;

    @FXML
    private TableColumn<CartItem, Double> unitPriceColumn;

    @FXML
    private TableColumn<CartItem, Double> totalPriceColumn;

    @FXML
    private Label totalLabel;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label vatLabel;

    @FXML private TextField couponField;
    @FXML private Label couponMessage;

    private Cart cart;

    private Coupon appliedCoupon;

    private final ObservableList<CartItem> cartItems =
            FXCollections.observableArrayList();

    public void setCart(Cart cart) {
        this.cart = cart;

        cartItems.setAll(cart.getItems());
        cartTable.setItems(cartItems);
        updateTotals();

    }

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
