package dao;

import database.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO
{
    public boolean isUsernameTaken(String username)
    {
        String sql = "SELECT id FROM userinfo WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
        catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean registerCustomer(User user) throws SQLException {

        String sql =
                "INSERT INTO userinfo (username, password, role, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getAddress());

            return stmt.executeUpdate() > 0;
        }
    }
}
