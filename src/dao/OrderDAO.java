package dao;

import database.DatabaseConnection;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public boolean placeOrderWithTransaction(User user, Cart cart, LocalDateTime deliveryTime, String usedCouponCode) {
        String insertOrder = "INSERT INTO orderinfo (user_id, total_cost, products, order_time, delivery_time, used_coupon_code) VALUES (?, ?, ?, NOW(), ?, ?)";
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
            stmt.setString(5, usedCouponCode);
            stmt.executeUpdate();

            // 🔹 Kuponu pasif hale getir (kullanılmış say)
            if (usedCouponCode != null && !usedCouponCode.isEmpty()) {
                new CouponDAO().markAsUsed(usedCouponCode);
            }

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

                // 🔹 Eksik olan satır (bunu ekle):
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

    /**
     * Cancels an order (sets iscancelled = TRUE)
     * Only works if the order is NOT delivered and NOT assigned to a carrier.
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

            // Ürün listesini çek
            try (PreparedStatement stmt = conn.prepareStatement(getProductsSQL)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        productsString = rs.getString("products");
                    }
                }
            }

            // Sipariş bulunamadıysa çık
            if (productsString == null || productsString.isEmpty()) {
                conn.rollback();
                return false;
            }

            // Ürünleri parse et (örnek format: "Apple x 2.0; Banana x 1.5;")
            String[] items = productsString.split(";");
            try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSQL)) {
                for (String item : items) {
                    item = item.trim();
                    if (item.isEmpty()) continue;
                    if (!item.contains("x")) continue;

                    // "Apple x 2.0" -> name=Apple, quantity=2.0
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

            // Siparişi iptal et
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
     * Returns all orders belonging to a specific customer (by user_id)
     */
    public List<Order> getOrdersByUser(int userId)
    {
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



}