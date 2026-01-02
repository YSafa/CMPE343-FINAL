package dao;

import database.DatabaseConnection;
import model.User;
import util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for users.
 * It manages user data in the database.
 */
public class UserDAO {

    /**
     * Adds a new user to the database.
     *
     * @param user user to add
     * @return true if user is added
     * @throws Exception if database error happens
     */
    public boolean addUser(User user) throws SQLException {
        String sql = "INSERT INTO userinfo (username, password, role, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, PasswordUtil.hashPassword(user.getPassword())); //hashli
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getAddress());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Registers a new customer.
     *
     * @param user customer user
     * @return true if registration is successful
     * @throws Exception if database error happens
     */
    public boolean registerCustomer(User user) throws SQLException {
        return addUser(user);
    }

    /**
     * Checks if a username already exists.
     *
     * @param username username to check
     * @return true if username is taken
     */
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT id FROM userinfo WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Gets all users with a specific role.
     *
     * @param role user role (customer, carrier, owner)
     * @return list of users
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM userinfo WHERE role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"), rs.getString("username"),
                    rs.getString("password"), rs.getString("role"),
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Deletes a user by ID.
     *
     * @param id user ID
     * @return true if delete is successful
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM userinfo WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}