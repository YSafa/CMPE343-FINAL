package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import util.Alertutil;
import util.PasswordUtil;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField addressField;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String address = addressField.getText().trim();

        // Boş alan kontrolü
        if (username.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Alertutil.showAlert(
                    "Error",
                    null,
                    "All fields are required.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        // Username uzunluk kontrolü
        if (username.length() < 4) {
            Alertutil.showAlert(
                    "Invalid Username",
                    null,
                    "Username must be at least 4 characters long.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // Şifre minimum uzunluk kontrolü
        if (password.length() < 4) {
            Alertutil.showAlert(
                    "Weak Password",
                    null,
                    "Password must be at least 4 characters long.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // Şifreyi hashle (SHA-256)
        String hashedPassword = PasswordUtil.hashPassword(password);

        if (hashedPassword == null) {
            Alertutil.showAlert(
                    "Error",
                    null,
                    "Password hashing failed. Please try again.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        try {
            // Yeni kullanıcı nesnesi oluştur
            User user = new User(
                    0,
                    username,
                    hashedPassword,
                    "customer",
                    address
            );

            boolean success = userDAO.registerCustomer(user);

            if (success) {
                Alertutil.showAlert(
                        "Success",
                        null,
                        "Registration successful! You can now log in.",
                        Alert.AlertType.INFORMATION
                );
                goBackToLogin();
            }

        } catch (SQLException e) {
            String msg = e.getMessage();

            if (msg != null && msg.toLowerCase().contains("duplicate") ||
                    msg.toLowerCase().contains("unique")) {
                Alertutil.showAlert(
                        "Registration Failed",
                        null,
                        "Username already exists. Please choose another one.",
                        Alert.AlertType.ERROR
                );
            } else {
                Alertutil.showAlert(
                        "Database Error",
                        null,
                        "An unexpected database error occurred.",
                        Alert.AlertType.ERROR
                );
            }

            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        goBackToLogin();
    }

    private void goBackToLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/Login.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
