package dao;

import database.DatabaseConnection;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public boolean placeOrderWithTransaction(User user, Cart cart, LocalDateTime deliveryTime) {
        String insertOrderSQL = "INSERT INTO orderinfo (ordertime, deliverytime, products, user_id, carrier_id, isdelivered, totalcost) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateStockSQL = "UPDATE productinfo SET stock = stock - ? WHERE id = ?";
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL)) {
                orderStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                orderStmt.setTimestamp(2, Timestamp.valueOf(deliveryTime));
                orderStmt.setString(3, cart.getCartContentAsString());
                orderStmt.setInt(4, user.getId());
                orderStmt.setInt(5, 0);
                orderStmt.setInt(6, 0);
                orderStmt.setDouble(7, cart.getTotalPriceWithVAT());
                orderStmt.executeUpdate();
            }

            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL)) {
                for (CartItem item : cart.getItems()) {
                    stockStmt.setDouble(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProduct().getId());
                    stockStmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public List<Order> getAllOrdersWithDetails() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username AS customerName, c.username AS carrierName " +
                     "FROM orderinfo o " +
                     "JOIN userinfo u ON o.user_id = u.id " +
                     "LEFT JOIN userinfo c ON o.carrier_id = c.id " +
                     "ORDER BY o.ordertime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order o = new Order(
                    rs.getInt("id"), rs.getTimestamp("ordertime"), rs.getTimestamp("deliverytime"),
                    rs.getString("products"), rs.getInt("user_id"), rs.getInt("carrier_id"),
                    rs.getBoolean("isdelivered"), rs.getDouble("totalcost"), ""
                );
                o.setCustomerName(rs.getString("customerName"));
                o.setCarrierName(rs.getString("carrierName") != null ? rs.getString("carrierName") : "Not Assigned");
                orders.add(o);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public List<Order> getUnassignedOrdersWithCustomerInfo() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username AS customerName, u.address AS customerAddress FROM orderinfo o JOIN userinfo u ON o.user_id = u.id WHERE o.isdelivered = 0 AND o.carrier_id = 0 ORDER BY o.deliverytime ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order o = new Order(rs.getInt("id"), rs.getTimestamp("ordertime"), rs.getTimestamp("deliverytime"), rs.getString("products"), rs.getInt("user_id"), rs.getInt("carrier_id"), rs.getBoolean("isdelivered"), rs.getDouble("totalcost"), "");
                o.setCustomerName(rs.getString("customerName"));
                o.setCustomerAddress(rs.getString("customerAddress"));
                orders.add(o);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public boolean assignOrderToCarrier(int orderId, int carrierId) {
        String sql = "UPDATE orderinfo SET carrier_id = ? WHERE id = ? AND carrier_id = 0 AND isdelivered = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Order> getAssignedOrdersForCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username AS customerName, u.address AS customerAddress FROM orderinfo o JOIN userinfo u ON o.user_id = u.id WHERE o.isdelivered = 0 AND o.carrier_id = ? ORDER BY o.deliverytime ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(rs.getInt("id"), rs.getTimestamp("ordertime"), rs.getTimestamp("deliverytime"), rs.getString("products"), rs.getInt("user_id"), rs.getInt("carrier_id"), rs.getBoolean("isdelivered"), rs.getDouble("totalcost"), "");
                    o.setCustomerName(rs.getString("customerName"));
                    o.setCustomerAddress(rs.getString("customerAddress"));
                    orders.add(o);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

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

    public List<Order> getDeliveredOrdersForCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username AS customerName, u.address AS customerAddress FROM orderinfo o JOIN userinfo u ON o.user_id = u.id WHERE o.isdelivered = 1 AND o.carrier_id = ? ORDER BY o.deliverytime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(rs.getInt("id"), rs.getTimestamp("ordertime"), rs.getTimestamp("deliverytime"), rs.getString("products"), rs.getInt("user_id"), rs.getInt("carrier_id"), rs.getBoolean("isdelivered"), rs.getDouble("totalcost"), "");
                    o.setCustomerName(rs.getString("customerName"));
                    o.setCustomerAddress(rs.getString("customerAddress"));
                    orders.add(o);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }
}