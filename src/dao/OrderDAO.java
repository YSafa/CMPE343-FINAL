package dao;

import database.DatabaseConnection;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for managing order data in the database.
 * It includes functions to create, update, cancel, and get orders.
 * All database actions are done using JDBC.
 */
public class OrderDAO {

    /**
     * Place a new order and use a transaction for safety.
     * If there is a coupon code, it will be marked as used.
     *
     * @param user The user who makes the order.
     * @param cart The shopping cart of the user.
     * @param deliveryTime The expected delivery time.
     * @return true if the order was placed successfully, false if there was an error.
     */
    public boolean placeOrderWithTransaction(User user, Cart cart, LocalDateTime deliveryTime) {
        String insertOrder = "INSERT INTO orderinfo (user_id, totalcost, products, ordertime, deliverytime) VALUES (?, ?, ?, NOW(), ?)";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            double total = cart.getTotalPrice();
            String products = cart.getCartContentAsString();

            PreparedStatement stmt = conn.prepareStatement(insertOrder);
            stmt.setInt(1, user.getId());
            stmt.setDouble(2, total);
            stmt.setString(3, products);
            stmt.setTimestamp(4, Timestamp.valueOf(deliveryTime));
            stmt.executeUpdate();


            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    /**
     * Get one order with all details (customer and carrier info).
     *
     * @param orderId The ID of the order.
     * @return An Order object with details, or null if not found.
     */
    public Order getOrderWithDetails(int orderId) {
        String sql = """
            SELECT o.*,
                   u.username AS customerName,
                   u.address AS customerAddress,
                   c.username AS carrierName
            FROM orderinfo o
            JOIN userinfo u ON o.user_id = u.id
            LEFT JOIN userinfo c ON o.carrier_id = c.id
            WHERE o.id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
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

                o.setCancelled(rs.getBoolean("iscancelled"));
                o.setCustomerName(rs.getString("customerName"));
                o.setCustomerAddress(rs.getString("customerAddress"));
                o.setCarrierName(rs.getString("carrierName"));
                return o;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all orders with customer and carrier information.
     *
     * @return A list of all orders with details.
     */
    public List<Order> getAllOrdersWithDetails() {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, u.username AS customerName, c.username AS carrierName
            FROM orderinfo o
            JOIN userinfo u ON o.user_id = u.id
            LEFT JOIN userinfo c ON o.carrier_id = c.id
            ORDER BY o.ordertime DESC
        """;

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

                o.setCancelled(rs.getBoolean("iscancelled"));
                o.setCustomerName(rs.getString("customerName"));
                o.setCarrierName(rs.getString("carrierName") != null ? rs.getString("carrierName") : "Not Assigned");
                orders.add(o);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Get all orders that do not have a carrier assigned yet.
     *
     * @return A list of unassigned orders with customer info.
     */
    public List<Order> getUnassignedOrdersWithCustomerInfo() {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, u.username AS customerName, u.address AS customerAddress
            FROM orderinfo o
            JOIN userinfo u ON o.user_id = u.id
            WHERE o.isdelivered = 0 
              AND o.carrier_id = 0 
              AND o.iscancelled = 0
            ORDER BY o.deliverytime ASC
        """;

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

                o.setCancelled(rs.getBoolean("iscancelled"));
                o.setCustomerName(rs.getString("customerName"));
                o.setCustomerAddress(rs.getString("customerAddress"));
                orders.add(o);
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return orders;
    }

    /**
     * Give (assign) one order to a carrier.
     *
     * @param orderId The ID of the order.
     * @param carrierId The ID of the carrier.
     * @return true if the order was assigned, false otherwise.
     */
    public boolean assignOrderToCarrier(int orderId, int carrierId) {
        String sql = "UPDATE orderinfo SET carrier_id = ? WHERE id = ? AND carrier_id = 0 AND isdelivered = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all active (not delivered) orders for a specific carrier.
     *
     * @param carrierId The ID of the carrier.
     * @return A list of assigned but not yet delivered orders.
     */
    public List<Order> getAssignedOrdersForCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, u.username AS customerName, u.address AS customerAddress
            FROM orderinfo o
            JOIN userinfo u ON o.user_id = u.id
            WHERE o.isdelivered = 0 
              AND o.iscancelled = 0
              AND o.carrier_id = ?
            ORDER BY o.deliverytime ASC
        """;

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

    /**
     * Mark an order as delivered by a carrier.
     *
     * @param orderId The order ID.
     * @param carrierId The carrier ID.
     * @param deliveredTime The delivery time.
     * @return true if updated successfully, false otherwise.
     */
    public boolean completeDelivery(int orderId, int carrierId, LocalDateTime deliveredTime) {
        String sql = "UPDATE orderinfo SET isdelivered = 1, deliverytime = ? WHERE id = ? AND carrier_id = ? AND isdelivered = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(deliveredTime));
            stmt.setInt(2, orderId);
            stmt.setInt(3, carrierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Get all orders that were delivered by a specific carrier.
     *
     * @param carrierId The carrier ID.
     * @return A list of delivered orders.
     */
    public List<Order> getDeliveredOrdersForCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username AS customerName, u.address AS customerAddress FROM orderinfo o JOIN userinfo u ON o.user_id = u.id WHERE o.isdelivered = 1 AND o.carrier_id = ? ORDER BY o.deliverytime DESC";

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

    /**
     * Cancel an order if it is not delivered and not assigned.
     * Also returns product quantities back to stock.
     *
     * @param orderId The order ID to cancel.
     * @return true if cancelled, false otherwise.
     */
    public boolean cancelOrder(int orderId) {
        String getProductsSQL = "SELECT products FROM orderinfo WHERE id = ? AND isdelivered = 0 AND iscancelled = 0 AND carrier_id = 0";
        String cancelOrderSQL = "UPDATE orderinfo SET iscancelled = 1 WHERE id = ?";
        String updateStockSQL = "UPDATE productinfo SET stock = stock + ? WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            String productsString = null;

            try (PreparedStatement stmt = conn.prepareStatement(getProductsSQL)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        productsString = rs.getString("products");
                    }
                }
            }

            if (productsString == null || productsString.isEmpty()) {
                conn.rollback();
                return false;
            }

            String[] items = productsString.split(";");
            try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSQL)) {
                for (String item : items) {
                    item = item.trim();
                    if (item.isEmpty() || !item.contains("x")) continue;

                    String[] parts = item.split("x");
                    if (parts.length == 2) {
                        String name = parts[0].trim();
                        try {
                            double quantity = Double.parseDouble(parts[1].trim().replace(",", "."));
                            updateStockStmt.setDouble(1, quantity);
                            updateStockStmt.setString(2, name);
                            updateStockStmt.executeUpdate();
                        } catch (NumberFormatException e) {
                            System.err.println("Could not parse quantity for item: " + item);
                        }
                    }
                }
            }

            try (PreparedStatement cancelStmt = conn.prepareStatement(cancelOrderSQL)) {
                cancelStmt.setInt(1, orderId);
                cancelStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Get all orders of a specific user.
     *
     * @param userId The user ID.
     * @return A list of the user's orders.
     */
    public List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orderinfo WHERE user_id = ? ORDER BY ordertime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
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
                    o.setCancelled(rs.getBoolean("iscancelled"));
                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }



    /**
     * Saves the Base64-encoded invoice into the invoice_content column.
     *
     * @param orderId ID of the order
     * @param base64Invoice Base64 encoded string of the invoice PDF
     * @return true if update was successful
     */
    public boolean saveInvoiceContent(int orderId, String base64Invoice) {
        String sql = "UPDATE orderinfo SET invoice_content = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, base64Invoice);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
