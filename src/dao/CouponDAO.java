package dao;

import database.DatabaseConnection;
import model.Coupon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages coupon data between the application and the database.
 * It allows creating, reading, and using coupons.
 * Coupons can be user-specific or general.
 */
public class CouponDAO {

    /**
     * Get all coupons that belong to a specific user.
     * If a coupon has user_id = NULL, it means it is general (for all users).
     *
     * @param userId the ID of the current user
     * @return a list of Coupon objects
     */
    public List<Coupon> getUserCoupons(int userId) {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM couponinfo WHERE user_id = ? OR user_id IS NULL ORDER BY expiration_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Coupon c = new Coupon(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getDouble("discount_rate"),
                        rs.getTimestamp("expiration_date"),
                        rs.getDouble("min_cart_value"),
                        rs.getBoolean("is_active"),
                        rs.getInt("user_id")
                );
                coupons.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coupons;
    }


    /**
     * Get a specific coupon object by its code.
     * Used when customer applies a coupon in the cart.
     *
     * @param code the coupon code
     * @return Coupon object if found and active, otherwise null
     */
    public Coupon getCouponByCode(String code) {
        String sql = "SELECT * FROM couponinfo WHERE code = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Coupon(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getDouble("discount_rate"),
                        rs.getTimestamp("expiration_date"),
                        rs.getDouble("min_cart_value"),
                        rs.getBoolean("is_active"),
                        rs.getInt("user_id")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Deactivate (disable) a coupon after it is used by a user.
     * Prevents using it again.
     *
     * @param couponCode the used coupon code
     * @return true if the update was successful
     */
    public boolean deactivateCouponForUser(String couponCode) {
        String sql = "UPDATE couponinfo SET is_active = 0 WHERE code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, couponCode);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Check if a coupon is valid and can be used by this user.
     * Coupon must be active, not expired, and meet the minimum cart value.
     *
     * @param code the coupon code typed by the user
     * @param userId the user's ID
     * @param cartTotal the total amount in the cart
     * @return discount rate if valid, otherwise -1
     */
    public double validateCoupon(String code, int userId, double cartTotal) {
        String sql = "SELECT * FROM couponinfo WHERE code = ? AND is_active = 1 " +
                "AND expiration_date > NOW() AND min_cart_value <= ? " +
                "AND (user_id IS NULL OR user_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setDouble(2, cartTotal);
            stmt.setInt(3, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("discount_rate");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Mark a coupon as used after it is successfully applied.
     * This prevents it from being used again.
     *
     * @param code the coupon code
     * @return true if the update was successful
     */
    public boolean markAsUsed(String code) {
        String sql = "UPDATE couponinfo SET is_active = 0, used_date = NOW() WHERE code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create a new coupon in the database.
     * Used by the owner to create manual coupons or by the system for rewards.
     *
     * @param code coupon code (must be unique)
     * @param rate discount rate in percent
     * @param daysValid number of days until expiration
     * @param minValue minimum cart value to use this coupon
     * @param userId owner of the coupon (can be NULL)
     * @return true if coupon created successfully
     */
    public boolean createCoupon(String code, double rate, int daysValid, double minValue, Integer userId) {
        String sql = "INSERT INTO couponinfo (code, discount_rate, expiration_date, min_cart_value, is_active, user_id) " +
                "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? DAY), ?, 1, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setDouble(2, rate);
            stmt.setInt(3, daysValid);
            stmt.setDouble(4, minValue);

            if (userId == null)
                stmt.setNull(5, Types.INTEGER);
            else
                stmt.setInt(5, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
