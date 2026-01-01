package controller;

import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Order;
import model.User;
import util.Alertutil;

public class CustomerOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> idColumn;
    @FXML private TableColumn<Order, String> productsColumn;
    @FXML private TableColumn<Order, String> orderTimeColumn;
    @FXML private TableColumn<Order, String> deliveryTimeColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Double> totalColumn;

    private final OrderDAO orderDAO = new OrderDAO();
    private User currentUser;

    /**
     * Sets the current logged-in user and loads their orders.
     * @param user Current user instance.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadOrders();
    }

    /**
     * Loads all orders of the current user into the table.
     */
    private void loadOrders() {
        try {
            var orders = FXCollections.observableArrayList(orderDAO.getOrdersByUser(currentUser.getId()));
            ordersTable.setItems(orders);

            idColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
            productsColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProducts()));
            orderTimeColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOrderTime().toString()));
            deliveryTimeColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDeliveryTime().toString()));
            statusColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(getStatusText(c.getValue())));
            totalColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTotalCost()));

        } catch (Exception e) {
            Alertutil.showErrorMessage("Failed to load orders: " + e.getMessage());
        }
    }

    /**
     * Returns a user-friendly text for the order status.
     */
    private String getStatusText(Order order) {
        if (order.isCancelled()) return "Cancelled ❌";
        if (order.isDelivered()) return "Delivered ✅";
        if (order.getCarrierId() == 0) return "Pending ⏳";
        return "Assigned to Carrier 🚚";
    }

    /**
     * Handles order cancellation — only undelivered, unassigned orders can be cancelled.
     */
    @FXML
    private void handleCancelOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alertutil.showWarningMessage("Please select an order to cancel.");
            return;
        }

        if (selected.isCancelled()) {
            Alertutil.showWarningMessage("This order is already cancelled.");
            return;
        }

        if (selected.isDelivered()) {
            Alertutil.showWarningMessage("Delivered orders cannot be cancelled.");
            return;
        }

        if (selected.getCarrierId() != 0) {
            Alertutil.showWarningMessage("This order has already been assigned to a carrier and cannot be cancelled.");
            return;
        }

        // Confirmation before cancelling
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to cancel this order?");
        if (confirm.showAndWait().get() != ButtonType.OK) return;

        boolean success = orderDAO.cancelOrder(selected.getId());

        if (success) {
            Alertutil.showSuccessMessage("Order cancelled successfully and stock restored.");
            loadOrders(); // tabloyu yenile
        } else {
            Alertutil.showErrorMessage("Failed to cancel order.");
        }
    }

    /**
     * Closes the order window.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        stage.close();
    }
}
