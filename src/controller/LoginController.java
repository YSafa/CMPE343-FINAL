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
                        rs.getString("address") // varsa
                );

                String role = loggedInUser.getRole();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader loader;

                switch (role) {
                    case "customer":
                        loader = new FXMLLoader(getClass().getResource("/resources/CustomerView.fxml"));
                        Scene scene = new Scene(loader.load());

                        CustomerController controller = loader.getController();
                        controller.setCurrentUser(loggedInUser);

                        stage.setScene(scene);
                        stage.show();
                        break;

                    case "carrier":
                        loader = new FXMLLoader(getClass().getResource("/resources/CarrierView.fxml"));
                        stage.setScene(new Scene(loader.load()));
                        stage.show();
                        break;

                    case "owner":
                        loader = new FXMLLoader(getClass().getResource("/resources/OwnerView.fxml"));
                        stage.setScene(new Scene(loader.load()));
                        stage.show();
                        break;

                    default:
                        showAlert("Error", "Unknown role!");
                }

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
