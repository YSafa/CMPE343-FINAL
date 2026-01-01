package model;

import java.sql.Timestamp;

public class Order {
    private int id;
    private Timestamp orderTime;
    private Timestamp deliveryTime;
    private String products;
    private int userId;
    private int carrierId;
    private boolean delivered;
    private double totalCost;
    private String customerName;
    private String customerAddress;
    private String carrierName;

    public Order() {}

    public Order(int id, Timestamp orderTime, Timestamp deliveryTime, String products,
                 int userId, int carrierId, boolean delivered, double totalCost, String invoiceContent) {
        this.id = id;
        this.orderTime = orderTime;
        this.deliveryTime = deliveryTime;
        this.products = products;
        this.userId = userId;
        this.carrierId = carrierId;
        this.delivered = delivered;
        this.totalCost = totalCost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    public Timestamp getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(Timestamp deliveryTime) { this.deliveryTime = deliveryTime; }

    public String getProducts() { return products; }
    public void setProducts(String products) { this.products = products; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCarrierId() { return carrierId; }
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }

    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
}