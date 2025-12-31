package dao;

import database.DatabaseConnection;
import model.Cart;
import model.CartItem;
import model.User;

import java.util.ArrayList;
import java.util.List;
import model.Order;
import java.sql.ResultSet;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class OrderDAO
{

    /**
     * Saves the order AND reduces stock simultaneously using a Transaction.
     * If an error occurs during stock reduction, the order is cancelled (Rollback).
     * @param user The customer placing the order.
     * @param cart The cart containing items.
     * @return true if both order insertion and stock updates are successful.
     */
    public boolean placeOrderWithTransaction(User user, Cart cart, LocalDateTime deliveryTime)
    {
        String insertOrderSQL =
                "INSERT INTO orderinfo (ordertime, deliverytime, products, user_id, carrier_id, isdelivered, totalcost) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateStockSQL = "UPDATE productinfo SET stock = stock - ? WHERE id = ?";

        Connection conn = null;

        try
        {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            // 1. DISABLE AUTO-COMMIT (Start Transaction)
            // This ensures changes are not saved until we say 'commit'.
            conn.setAutoCommit(false);

            // --- STEP A: INSERT ORDER ---
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL))
            {
                orderStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                orderStmt.setTimestamp(2, Timestamp.valueOf(deliveryTime));
                orderStmt.setString(3, cart.getCartContentAsString());
                orderStmt.setInt(4, user.getId());
                orderStmt.setInt(5, 0); // carrier_id
                orderStmt.setInt(6, 0); // isdelivered
                orderStmt.setDouble(7, cart.getTotalPriceWithVAT());


                orderStmt.executeUpdate();
            }

            // --- STEP B: REDUCE STOCK (Loop through items) ---
            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL))
            {
                for (CartItem item : cart.getItems()) {
                    // Set amount to subtract
                    stockStmt.setDouble(1, item.getQuantity());
                    // Set product ID
                    stockStmt.setInt(2, item.getProduct().getId());

                    // Execute update for this item
                    stockStmt.executeUpdate();
                }
            }

            // 2. COMMIT IF SUCCESSFUL
            // If code reaches here, it means no errors occurred. Save everything.
            conn.commit();
            return true;

        } catch (SQLException e)
        {
            e.printStackTrace();
            // 3. ROLLBACK IF ERROR OCCURS
            // If any error happened above, undo all changes.
            if (conn != null)
            {
                try {
                    System.err.println("Error occurred! Rolling back changes...");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // 4. RESET CONNECTION STATE AND CLOSE
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Always set back to true before closing
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Fetches all orders that have NOT been delivered yet.
     * Used for the Carrier screen.
     * @return A list of undelivered Order objects.
     */
    public java.util.List<model.Order> getUndeliveredOrders()
    {
        java.util.List<model.Order> orders = new java.util.ArrayList<>();
        String sql = "SELECT * FROM orderinfo WHERE isdelivered = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {
                // Map the ResultSet to an Order object
                // Note: Ensure your Order model constructor matches these fields
                model.Order order = new model.Order(
                        rs.getInt("id"),
                        rs.getTimestamp("ordertime"),
                        rs.getTimestamp("deliverytime"),
                        rs.getString("products"), // Returns the product string (e.g. "Apple x 2")
                        rs.getInt("user_id"),
                        rs.getInt("carrier_id"),
                        rs.getBoolean("isdelivered"), // tinyint(1) converts to boolean
                        rs.getDouble("totalcost"),
                        ""
                );
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    public List<model.Order> getAllOrders() {
        List<model.Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orderinfo";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(new model.Order(
                    rs.getInt("id"), rs.getTimestamp("ordertime"),
                    rs.getTimestamp("deliverytime"), rs.getString("products"),
                    rs.getInt("user_id"), rs.getInt("carrier_id"),
                    rs.getBoolean("isdelivered"), rs.getDouble("totalcost"), ""
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }
    public List<Order> getUnassignedOrdersWithCustomerInfo() {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT o.*, u.username AS customerName, u.address AS customerAddress " +
                        "FROM orderinfo o " +
                        "JOIN userinfo u ON o.user_id = u.id " +
                        "WHERE o.isdelivered = 0 AND o.carrier_id = 0 " +
                        "ORDER BY o.deliverytime ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order o = new Order(
                        rs.getInt("id"),
                        rs.getTimestamp("ordertime"),
                        rs.getTimestamp("deliverytime"),
                        rs.getString("products"),
                        rs.getInt("user_id"),
                        rs.getInt("carrier_id"),
                        rs.getBoolean("isdelivered"),
                        rs.getDouble("totalcost"),
                        ""
                );
                o.setCustomerName(rs.getString("customerName"));
                o.setCustomerAddress(rs.getString("customerAddress"));
                orders.add(o);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return orders;
    }
    public boolean assignOrderToCarrier(int orderId, int carrierId) {
        String sql =
                "UPDATE orderinfo " +
                        "SET carrier_id = ? " +
                        "WHERE id = ? AND carrier_id = 0 AND isdelivered = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0; // 0 ise: biri senden önce aldı
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Order> getAssignedOrdersForCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT o.*, u.username AS customerName, u.address AS customerAddress " +
                        "FROM orderinfo o " +
                        "JOIN userinfo u ON o.user_id = u.id " +
                        "WHERE o.isdelivered = 0 AND o.carrier_id = ? " +
                        "ORDER BY o.deliverytime ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carrierId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(
                            rs.getInt("id"),
                            rs.getTimestamp("ordertime"),
                            rs.getTimestamp("deliverytime"),
                            rs.getString("products"),
                            rs.getInt("user_id"),
                            rs.getInt("carrier_id"),
                            rs.getBoolean("isdelivered"),
                            rs.getDouble("totalcost"),
                            ""
                    );
                    o.setCustomerName(rs.getString("customerName"));
                    o.setCustomerAddress(rs.getString("customerAddress"));
                    orders.add(o);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return orders;
    }
    public List<Order> getDeliveredOrdersForCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT o.*, u.username AS customerName, u.address AS customerAddress " +
                        "FROM orderinfo o " +
                        "JOIN userinfo u ON o.user_id = u.id " +
                        "WHERE o.isdelivered = 1 AND o.carrier_id = ? " +
                        "ORDER BY o.deliverytime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carrierId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(
                            rs.getInt("id"),
                            rs.getTimestamp("ordertime"),
                            rs.getTimestamp("deliverytime"),
                            rs.getString("products"),
                            rs.getInt("user_id"),
                            rs.getInt("carrier_id"),
                            rs.getBoolean("isdelivered"),
                            rs.getDouble("totalcost"),
                            ""
                    );
                    o.setCustomerName(rs.getString("customerName"));
                    o.setCustomerAddress(rs.getString("customerAddress"));
                    orders.add(o);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return orders;
    }
    public boolean completeDelivery(int orderId, int carrierId, LocalDateTime deliveredTime) {
        String sql =
                "UPDATE orderinfo " +
                        "SET isdelivered = 1, deliverytime = ? " +
                        "WHERE id = ? AND carrier_id = ? AND isdelivered = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(deliveredTime));
            stmt.setInt(2, orderId);
            stmt.setInt(3, carrierId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}