package controller;

import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Order;
import model.User;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class CarrierController {

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

    public void setCurrentCarrier(User carrier) {
        this.currentCarrier = carrier;
        refreshAll();
    }

    @FXML
    public void initialize() {
        // hour options
        for (int h = 0; h < 24; h++) deliveredHourBox.getItems().add(h);
        deliveredHourBox.getSelectionModel().select(LocalTime.now().getHour());

        // Available columns
        colAvailId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colAvailProducts.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("products"));
        colAvailCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCustomerName()
        ));
        colAvailAddress.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCustomerAddress()
        ));
        colAvailTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
        colAvailDelivery.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDeliveryTime().toString()
        ));

        // Selected columns
        colSelId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colSelProducts.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("products"));
        colSelCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCustomerName()
        ));
        colSelAddress.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCustomerAddress()
        ));
        colSelTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
        colSelDelivery.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDeliveryTime().toString()
        ));

        // Completed columns
        colComId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colComProducts.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("products"));
        colComCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCustomerName()
        ));
        colComTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
        colComDelivered.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDeliveryTime().toString()
        ));
    }

    private void refreshAll() {
        if (currentCarrier == null) return;

        tableAvailable.setItems(FXCollections.observableArrayList(
                orderDAO.getUnassignedOrdersWithCustomerInfo()
        ));

        tableSelected.setItems(FXCollections.observableArrayList(
                orderDAO.getAssignedOrdersForCarrier(currentCarrier.getId())
        ));

        tableCompleted.setItems(FXCollections.observableArrayList(
                orderDAO.getDeliveredOrdersForCarrier(currentCarrier.getId())
        ));
    }

    @FXML
    private void handleClaimSelectedAvailable() {
        if (currentCarrier == null) { show("Carrier session missing."); return; }

        Order selected = tableAvailable.getSelectionModel().getSelectedItem();
        if (selected == null) { show("Select an available order first."); return; }

        boolean ok = orderDAO.assignOrderToCarrier(selected.getId(), currentCarrier.getId());
        if (!ok) {
            show("This order was already claimed by another carrier.");
            refreshAll();
            return;
        }

        show("Order claimed successfully.");
        refreshAll();
    }

    @FXML
    private void handleCompleteSelectedOrder() {
        if (currentCarrier == null) { show("Carrier session missing."); return; }

        Order selected = tableSelected.getSelectionModel().getSelectedItem();
        if (selected == null) { show("Select an order from 'Selected' tab."); return; }

        if (deliveredDatePicker.getValue() == null || deliveredHourBox.getValue() == null) {
            show("Please select delivered date and hour.");
            return;
        }

        LocalDateTime deliveredTime = LocalDateTime.of(
                deliveredDatePicker.getValue(),
                LocalTime.of(deliveredHourBox.getValue(), 0)
        );

        boolean ok = orderDAO.completeDelivery(selected.getId(), currentCarrier.getId(), deliveredTime);
        if (!ok) {
            show("Failed. Make sure this order is assigned to you and not already delivered.");
            refreshAll();
            return;
        }

        show("Order marked as delivered.");
        deliveredDatePicker.setValue(null);
        deliveredHourBox.getSelectionModel().select(LocalTime.now().getHour());
        refreshAll();
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
    }

    private void show(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}
