package model;

import java.sql.Timestamp;

/**
 * Model class for carrier rating.
 * It stores rating and comment information.
 */
public class CarrierRating {

    private int id;
    private int orderId;
    private int customerId;
    private int carrierId;
    private int rating;
    private String comment;
    private Timestamp createdAt;

    /**
     * Creates a carrier rating object.
     *
     * @param id rating ID
     * @param orderId order ID
     * @param customerId customer ID
     * @param carrierId carrier ID
     * @param rating rating value
     * @param comment rating comment
     * @param createdAt rating date
     */
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
    /**
     * Gets the order ID.
     *
     * @return order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Gets the carrier ID.
     *
     * @return carrier ID
     */
    public int getCarrierId() {
        return carrierId;
    }

    /**
     * Gets the rating value.
     *
     * @return rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * Gets the rating comment.
     *
     * @return comment text
     */
    public String getComment() {
        return comment;
    }
}
