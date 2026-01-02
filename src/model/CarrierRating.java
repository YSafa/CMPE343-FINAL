package model;

import java.sql.Timestamp;

public class CarrierRating {

    private int id;
    private int orderId;
    private int customerId;
    private int carrierId;
    private int rating;
    private String comment;
    private Timestamp createdAt;

    public CarrierRating(int id, int orderId, int customerId,
                         int carrierId, int rating, String comment,
                         Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getOrderId() { return orderId; }
    public int getCarrierId() { return carrierId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}
