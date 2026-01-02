package dao;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RatingDAO {

    // Bu sipariş daha önce puanlanmış mı?
    public boolean hasRatingForOrder(int orderId) {
        String sql = "SELECT id FROM carrier_ratings WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Yeni rating ekle
    public boolean addRating(int orderId, int customerId,
                             int carrierId, int rating, String comment) {

        String sql = """
            INSERT INTO carrier_ratings
            (order_id, customer_id, carrier_id, rating, comment)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, customerId);
            ps.setInt(3, carrierId);
            ps.setInt(4, rating);
            ps.setString(5, comment);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            // UNIQUE(order_id) ihlali burada patlar → zaten puanlanmış
            e.printStackTrace();
            return false;
        }
    }
}
