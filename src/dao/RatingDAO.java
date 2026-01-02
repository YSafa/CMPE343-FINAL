package dao;

import database.DatabaseConnection;
import java.sql.*;

public class RatingDAO {

    public boolean hasRatingForOrder(int orderId) {
        String sql = "SELECT 1 FROM carrier_ratings WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
    public List<String> getCommentsForCarrier(int carrierId) {
        List<String> comments = new ArrayList<>();

        String sql = """
        SELECT comment
        FROM carrier_ratings
        WHERE carrier_id = ?
          AND comment IS NOT NULL
          AND comment <> ''
        ORDER BY created_at DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(rs.getString("comment"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return comments;
    }

    public boolean addRating(int orderId, int customerId, int carrierId,
                             int rating, String comment) {

        String sql = """
            INSERT INTO carrier_ratings
            (order_id, customer_id, carrier_id, rating, comment)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, carrierId);
            stmt.setInt(4, rating);
            stmt.setString(5, comment);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getAverageRatingForCarrier(int carrierId) {
        String sql = """
            SELECT AVG(rating)
            FROM carrier_ratings
            WHERE carrier_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getRatingCountForCarrier(int carrierId) {
        String sql = """
            SELECT COUNT(*)
            FROM carrier_ratings
            WHERE carrier_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
