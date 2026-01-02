package controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import database.DatabaseConnection;
import model.User;
import util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginBox;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        resetStyles();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty!");
            return;
        }

        String sql = "SELECT * FROM userinfo WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password"); // bu hashli haldir
                String inputHash = PasswordUtil.hashPassword(password); //kullanıcı girişi hashleniyor

                if (inputHash != null && inputHash.equals(dbPassword)) { // hash eşleşirse giriş başarılı
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("address")
                    );
                    loadDashboard(user);
                } else {
                    showError("Incorrect password!");
                }
            } else {
                showError("Invalid credentials!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Connection error occurred!");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);

        usernameField.setStyle("-fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        passwordField.setStyle("-fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        TranslateTransition tt = new TranslateTransition(Duration.millis(50), loginBox);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void resetStyles() {
        errorLabel.setText("");
        usernameField.setStyle("-fx-border-color: #dcdcdc; -fx-border-radius: 10; -fx-background-radius: 10;");
        passwordField.setStyle("-fx-border-color: #dcdcdc; -fx-border-radius: 10; -fx-background-radius: 10;");
    }

    private void loadDashboard(User user) throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        String fxml = "";

        switch (user.getRole().toLowerCase()) {
            case "customer": fxml = "/resources/CustomerView.fxml"; break;
            case "carrier": fxml = "/resources/CarrierView.fxml"; break;
            case "owner": fxml = "/resources/OwnerView.fxml"; break;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();

        if (user.getRole().equalsIgnoreCase("customer")) {
            ((CustomerController) loader.getController()).setCurrentUser(user);
        } else if (user.getRole().equalsIgnoreCase("carrier")) {
            ((CarrierController) loader.getController()).setCurrentCarrier(user);
        }

        stage.setScene(new Scene(root, 960, 540));
        stage.centerOnScreen();
    }

    @FXML
    private void handleOpenRegister() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/Register.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 960, 540));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Register screen could not be opened!");
        }

    }

}
