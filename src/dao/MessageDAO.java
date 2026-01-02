package dao;

import database.DatabaseConnection;
import model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for messages.
 * It sends messages and gets conversations from the database.
 */
public class MessageDAO {

    /**
     * Saves a new message to the database.
     *
     * @param senderId sender user ID
     * @param receiverId receiver user ID
     * @param text message content
     */
    public void sendMessage(int senderId, int receiverId, String text) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, text);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all messages between two users.
     * Messages are ordered by send time.
     *
     * @param userA first user ID
     * @param userB second user ID
     * @return list of messages between users
     */
    public List<Message> getConversation(int userA, int userB) {
        List<Message> list = new ArrayList<>();
        String sql = """
            SELECT * FROM messages
            WHERE (sender_id=? AND receiver_id=?)
               OR (sender_id=? AND receiver_id=?)
            ORDER BY sent_at
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Message(
                        rs.getInt("id"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getString("message"),
                        rs.getTimestamp("sent_at").toLocalDateTime(),
                        rs.getBoolean("is_read")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
