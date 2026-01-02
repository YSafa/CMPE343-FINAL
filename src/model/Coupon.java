package model;

import java.sql.Timestamp;

/**
 * This class represents a coupon in the system.
 * A coupon gives a discount to the user when shopping.
 * It can be for one user or general for all users.
 */
public class Coupon {

    private int id;
    private String code;
    private double discountRate;
    private Timestamp expirationDate;
    private double minCartValue;
    private boolean isActive;
    private int userId;

    /**
     * Create a Coupon object with all fields.
     *
     * @param id coupon id
     * @param code coupon code
     * @param discountRate discount rate in percent
     * @param expirationDate expiration date and time
     * @param minCartValue minimum cart value to use
     * @param isActive true if coupon is active
     * @param userId id of the user who owns the coupon (0 means general)
     */
    public Coupon(int id, String code, double discountRate, Timestamp expirationDate,
                  double minCartValue, boolean isActive, int userId) {
        this.id = id;
        this.code = code;
        this.discountRate = discountRate;
        this.expirationDate = expirationDate;
        this.minCartValue = minCartValue;
        this.isActive = isActive;
        this.userId = userId;
    }

    /** Empty constructor for flexibility. */
    public Coupon() {}

    /** Get the coupon id. */
    public int getId() { return id; }

    /** Set the coupon id. */
    public void setId(int id) { this.id = id; }

    /** Get the coupon code. */
    public String getCode() { return code; }

    /** Set the coupon code. */
    public void setCode(String code) { this.code = code; }

    /** Get the discount rate. */
    public double getDiscountRate() { return discountRate; }

    /** Set the discount rate. */
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }

    /** Get the expiration date. */
    public Timestamp getExpirationDate() { return expirationDate; }

    /** Set the expiration date. */
    public void setExpirationDate(Timestamp expirationDate) { this.expirationDate = expirationDate; }

    /** Get the minimum cart value required. */
    public double getMinCartValue() { return minCartValue; }

    /** Set the minimum cart value. */
    public void setMinCartValue(double minCartValue) { this.minCartValue = minCartValue; }

    /** Check if the coupon is active. */
    public boolean isActive() { return isActive; }

    /** Set the active state of the coupon. */
    public void setActive(boolean active) { isActive = active; }

    /** Get the user ID who owns the coupon. */
    public int getUserId() { return userId; }

    /** Set the user ID of the coupon owner. */
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discountRate=" + discountRate +
                ", expirationDate=" + expirationDate +
                ", minCartValue=" + minCartValue +
                ", isActive=" + isActive +
                ", userId=" + userId +
                '}';
    }
}
