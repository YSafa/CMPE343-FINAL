package model;

import java.sql.Timestamp; // For datetime columns

public class Order
{
    private int id;
    private Timestamp orderTime;
    private Timestamp deliveryTime;
    private String products; // Stored as text (e.g., JSON or comma-separated)
    private int userId;
    private int carrierId;
    private boolean isDelivered; // tinyint(1) -> boolean
    private double totalCost;
    //private String invoiceContent;

    public Order(int id, Timestamp orderTime, Timestamp deliveryTime, String products,
                 int userId, int carrierId, boolean isDelivered, double totalCost, String invoiceContent)
    {
        this.id = id;
        this.orderTime = orderTime;
        this.deliveryTime = deliveryTime;
        this.products = products;
        this.userId = userId;
        this.carrierId = carrierId;
        this.isDelivered = isDelivered;
        this.totalCost = totalCost;
        //this.invoiceContent = invoiceContent;
    }

    // Getters...
    public int getId() { return id; }
    public Timestamp getOrderTime() { return orderTime; }
    public Timestamp getDeliveryTime() { return deliveryTime; }
    public String getProducts() { return products; }
    public int getUserId() { return userId; }
    public int getCarrierId() { return carrierId; }
    public boolean getIsDelivered() { return isDelivered; }
    public double getTotalCost() { return totalCost; }
    // ... add other getters as needed
}