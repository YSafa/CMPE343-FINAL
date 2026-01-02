package controller;

import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Order;
import model.User;
import util.Alertutil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Controller class for the Carrier Panel (Delivery Person interface).
 * This class allows carriers to see available orders,
 * claim them, complete deliveries, and log out of the system.
 */
public class CarrierController {

    // --- UI Components ---
    @FXML private TableView<Order> tableAvailable;
    @FXML private TableColumn<Order, Integer> colAvailId;
    @FXML private TableColumn<Order, String> colAvailProducts;
    @FXML private TableColumn<Order, String> colAvailCustomer;
    @FXML private TableColumn<Order, String> colAvailAddress;
    @FXML private TableColumn<Order, Double> colAvailTotal;
    @FXML private TableColumn<Order, String> colAvailDelivery;

    @FXML private TableView<Order> tableSelected;
    @FXML private TableColumn<Order, Integer> colSelId;
    @FXML private TableColumn<Order, String> colSelProducts;
    @FXML private TableColumn<Order, String> colSelCustomer;
    @FXML private TableColumn<Order, String> colSelAddress;
    @FXML private TableColumn<Order, Double> colSelTotal;
    @FXML private TableColumn<Order, String> colSelDelivery;

    @FXML private TableView<Order> tableCompleted;
    @FXML private TableColumn<Order, Integer> colComId;
    @FXML private TableColumn<Order, String> colComProducts;
    @FXML private TableColumn<Order, String> colComCustomer;
    @FXML private TableColumn<Order, Double> colComTotal;
    @FXML private TableColumn<Order, String> colComDelivered;

    @FXML private DatePicker deliveredDatePicker;
    @FXML private ComboBox<Integer> deliveredHourBox;

    private final OrderDAO orderDAO = new OrderDAO();
    private User currentCarrier;

    /**
     * Sets the current logged-in carrier and refreshes the order tables.
     *
     * @param carrier The current carrier user.
     */
    public void setCurrentCarrier(User carrier) {
        this.currentCarrier = carrier;
        refreshAll();
    }

    /**
     * Initializes the controller.
     * This method runs automatically when the FXML is loaded.
     * It sets up table columns and fills the delivery hour combo box.
     */
    @FXML
    public void initialize() {
        for (int h = 0; h < 24; h++) deliveredHourBox.getItems().add(h);
        deliveredHourBox.getSelectionModel().select(LocalTime.now().getHour());

        colAvailId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colAvailProducts.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("products"));
        colAvailCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomerName()));
        colAvailAddress.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomerAddress()));
        colAvailTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
        colAvailDelivery.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDeliveryTime().toString()));

        colSelId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colSelProducts.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("products"));
        colSelCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomerName()));
        colSelAddress.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomerAddress()));
        colSelTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
        colSelDelivery.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDeliveryTime().toString()));

        colComId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colComProducts.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("products"));
        colComCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomerName()));
        colComTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
        colComDelivered.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDeliveryTime().toString()));
    }

    /**
     * Refreshes all order tables:
     * - Available orders (not assigned)
     * - Selected (assigned to this carrier)
     * - Completed (delivered by this carrier)
     */
    private void refreshAll() {
        if (currentCarrier == null) return;
        tableAvailable.setItems(FXCollections.observableArrayList(orderDAO.getUnassignedOrdersWithCustomerInfo()));
        tableSelected.setItems(FXCollections.observableArrayList(orderDAO.getAssignedOrdersForCarrier(currentCarrier.getId())));
        tableCompleted.setItems(FXCollections.observableArrayList(orderDAO.getDeliveredOrdersForCarrier(currentCarrier.getId())));
    }

    /**
     * Handles the action when the carrier clicks "Claim" on an available order.
     * The selected order will be assigned to the current carrier if possible.
     */
    @FXML
    private void handleClaimSelectedAvailable() {
        if (currentCarrier == null) {
            Alertutil.showErrorMessage("Carrier session missing.");
            return;
        }

        Order selected = tableAvailable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alertutil.showWarningMessage("Select an available order first.");
            return;
        }

        if (orderDAO.assignOrderToCarrier(selected.getId(), currentCarrier.getId())) {
            Alertutil.showSuccessMessage("Order claimed successfully.");
            refreshAll();
        } else {
            Alertutil.showErrorMessage("This order was already claimed.");
            refreshAll();
        }
    }

    /**
     * Handles the action when the carrier completes an order.
     * Marks the selected order as delivered with the chosen date and hour.
     */
    @FXML
    private void handleCompleteSelectedOrder() {
        if (currentCarrier == null) return;

        Order selected = tableSelected.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alertutil.showWarningMessage("Select an order from 'Selected' tab.");
            return;
        }

        if (deliveredDatePicker.getValue() == null) {
            Alertutil.showWarningMessage("Please select delivered date.");
            return;
        }

        LocalDateTime deliveredTime = LocalDateTime.of(
                deliveredDatePicker.getValue(),
                LocalTime.of(deliveredHourBox.getValue(), 0)
        );

        if (orderDAO.completeDelivery(selected.getId(), currentCarrier.getId(), deliveredTime)) {
            Alertutil.showSuccessMessage("Order marked as delivered.");
            deliveredDatePicker.setValue(null);
            refreshAll();
        }
    }

    /**
     * Logs out the current carrier and returns to the login screen.
     * Shows a confirmation dialog before logout.
     */
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
                Stage currentStage = (Stage) tableAvailable.getScene().getWindow();
                currentStage.setScene(new Scene(loginRoot));
                currentStage.centerOnScreen();
            } catch (IOException e) {
                Alertutil.showErrorMessage("Logout failed: " + e.getMessage());
            }
        }
    }

    /**
     * Refresh button action.
     * Reloads all tables and updates the view.
     */
    @FXML
    private void handleRefresh() {
        refreshAll();
    }
}
