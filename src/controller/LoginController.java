package controller;

import javafx.fxml.FXML;
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

        String sql = "SELECT role FROM userinfo WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");

                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader loader;

                switch (role) {
                    case "customer":
                        loader = new FXMLLoader(getClass().getResource("/fxml/CustomerView.fxml"));
                        break;
                    case "carrier":
                        loader = new FXMLLoader(getClass().getResource("/fxml/CarrierView.fxml"));
                        break;
                    case "owner":
                        loader = new FXMLLoader(getClass().getResource("/fxml/OwnerView.fxml"));
                        break;
                    default:
                        showAlert("Error", "Unknown role!");
                        return;
                }

                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.show();



            } else {
                showAlert("Error", "Invalid username or password!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while logging in.");
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
