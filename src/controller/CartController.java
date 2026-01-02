package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Cart;
import model.CartItem;

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

    private Cart cart;


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
        updateTotals();
    }

    @FXML
    private void handleClose() {
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

    private void updateTotals() {

        double subtotal = 0.0;

        for (CartItem item : cartItems) {
            subtotal += item.getTotalItemPrice();
        }

        double vatAmount = subtotal * VAT_RATE;
        double total = subtotal + vatAmount;

        subtotalLabel.setText(String.format("Subtotal: ₺%.2f", subtotal));
        vatLabel.setText(String.format("VAT (18%%): ₺%.2f", vatAmount));
        totalLabel.setText(String.format("Total (incl. VAT): ₺%.2f", total));
    }



}
