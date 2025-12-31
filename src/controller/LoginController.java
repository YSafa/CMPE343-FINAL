package controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password cannot be empty!");
            return;
        }

        String sql = "SELECT * FROM userinfo WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User loggedInUser = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("address")
                );

                String role = loggedInUser.getRole();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader loader;
                Parent root;
                Scene scene;

                switch (role) {
                    case "customer":
                        loader = new FXMLLoader(getClass().getResource("/resources/CustomerView.fxml"));
                        root = loader.load();
                        scene = new Scene(root, 960, 540); 
                        
                        CustomerController customerController = loader.getController();
                        customerController.setCurrentUser(loggedInUser);

                        stage.setTitle("Group29 GreenGrocer");
                        stage.setScene(scene);
                        stage.centerOnScreen(); // Ekranda ortalanmalı 
                        stage.show();
                        break;

                    case "carrier":
                        loader = new FXMLLoader(getClass().getResource("/resources/CarrierView.fxml"));
                        root = loader.load();
                        scene = new Scene(root, 960, 540); 
                        
                        stage.setTitle("Group29 GreenGrocer");
                        stage.setScene(scene);
                        stage.centerOnScreen();
                        stage.show();
                        break;

                    case "owner":
                        loader = new FXMLLoader(getClass().getResource("/resources/OwnerView.fxml"));
                        root = loader.load();
                        scene = new Scene(root, 960, 540); 
                        
                        stage.setTitle("Group29 GreenGrocer");
                        stage.setScene(scene);
                        stage.centerOnScreen();
                        stage.show();
                        break;

                    default:
                        showAlert("Error", "Unknown role!");
                }

            } else {
                showAlert("Login Failed", "Invalid username or password!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while logging in.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}