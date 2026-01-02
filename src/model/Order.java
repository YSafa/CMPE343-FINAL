package model;

import java.sql.Timestamp;

/**
 * Represents an order in the system.
 * It stores order, customer, carrier, and status information.
 */
public class Order {
    private int id;
    private Timestamp orderTime;
    private Timestamp deliveryTime;
    private String products;
    private int userId;
    private int carrierId;
    private boolean delivered;
    private boolean isCancelled;
    private double totalCost;
    private String customerName;
    private String customerAddress;
    private String carrierName;

    /**
     * Empty constructor.
     */
    public Order() {}

    /**
     * Creates an order with basic information.
     *
     * @param id order ID
     * @param orderTime order time
     * @param deliveryTime delivery time
     * @param products product list as text
     * @param userId customer ID
     * @param carrierId carrier ID
     * @param delivered delivery status
     * @param totalCost total order cost
     * @param extra unused parameter
     */
    public Order(int id, Timestamp orderTime, Timestamp deliveryTime, String products,
                 int userId, int carrierId, boolean delivered, double totalCost, String extra) {
        this.id = id;
        this.orderTime = orderTime;
        this.deliveryTime = deliveryTime;
        this.products = products;
        this.userId = userId;
        this.carrierId = carrierId;
        this.delivered = delivered;
        this.totalCost = totalCost;
    }


    /** @return order ID */
    public int getId() { return id; }

    /** @param id order ID */
    public void setId(int id) { this.id = id; }

    /** @return order time */
    public Timestamp getOrderTime() { return orderTime; }

    /** @param orderTime order time */
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    /** @return delivery time */
    public Timestamp getDeliveryTime() { return deliveryTime; }

    /** @param deliveryTime delivery time */
    public void setDeliveryTime(Timestamp deliveryTime) { this.deliveryTime = deliveryTime; }

    /** @return product list text */
    public String getProducts() { return products; }

    /** @param products product list text */
    public void setProducts(String products) { this.products = products; }

    /** @return customer user ID */
    public int getUserId() { return userId; }

    /** @param userId customer user ID */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return carrier ID */
    public int getCarrierId() { return carrierId; }

    /** @param carrierId carrier ID */
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }

    /** @return true if delivered */
    public boolean isDelivered() { return delivered; }

    /** @param delivered delivery status */
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    /** @return true if cancelled */
    public boolean isCancelled() { return isCancelled; }

    /** @param cancelled cancel status */
    public void setCancelled(boolean cancelled) { this.isCancelled = cancelled; }

    /** @return total cost */
    public double getTotalCost() { return totalCost; }

    /** @param totalCost total cost */
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    /** @return customer name */
    public String getCustomerName() { return customerName; }

    /** @param customerName customer name */
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    /** @return customer address */
    public String getCustomerAddress() { return customerAddress; }

    /** @param customerAddress customer address */
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    /** @return carrier name */
    public String getCarrierName() { return carrierName; }

    /** @param carrierName carrier name */
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }

    /**
     * Gets the current order status as text.
     *
     * @return order status
     */
    public String getStatus() {
        if (isCancelled) return "Cancelled ❌";
        if (delivered) return "Delivered ✅";
        if (carrierId > 0) return "Assigned to Carrier 🚚";
        return "Pending ⏳";
    }

}