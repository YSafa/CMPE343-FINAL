package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Control;
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
    private PasswordField confirmPasswordField;

    @FXML
    private TextField addressField;

    @FXML
    private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    private final String defaultStyle = "-fx-border-color: #dcdcdc; -fx-border-radius: 10; -fx-background-radius: 10;";
    private final String errorStyle = "-fx-border-color: #ff4d4d; -fx-border-width: 2px; -fx-border-radius: 10; -fx-background-radius: 10;";

    @FXML
    private void handleRegister() {
        resetStyles();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String address = addressField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || address.isEmpty()) {
            if (username.isEmpty()) markAsError(usernameField);
            if (password.isEmpty()) markAsError(passwordField);
            if (confirmPassword.isEmpty()) markAsError(confirmPasswordField);
            if (address.isEmpty()) markAsError(addressField);
            errorLabel.setText("All fields are required.");
            return;
        }

        if (username.length() < 4 || !username.matches("[a-zA-Z0-9]+")) {
            markAsError(usernameField);
            errorLabel.setText("Username: Min 4 chars, no symbols.");
            return;
        }

        if (password.length() < 4 || !password.matches("[a-zA-Z0-9]+")) {
            markAsError(passwordField);
            errorLabel.setText("Password: Min 4 chars, no symbols.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            markAsError(passwordField);
            markAsError(confirmPasswordField);
            errorLabel.setText("Passwords do not match.");
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        if (hashedPassword == null) {
            errorLabel.setText("Hashing failed.");
            return;
        }

        try {
            User user = new User(0, username, hashedPassword, "customer", address);
            if (userDAO.registerCustomer(user)) {
                Alertutil.showAlert("Success", null, "Registration successful!", Alert.AlertType.INFORMATION);
                goBackToLogin();
            }
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.toLowerCase().contains("duplicate") || msg.toLowerCase().contains("unique"))) {
                markAsError(usernameField);
                errorLabel.setText("Username already exists.");
            } else {
                errorLabel.setText("Database error occurred.");
            }
        }
    }

    private void markAsError(Control field) {
        field.setStyle(errorStyle);
    }

    private void resetStyles() {
        usernameField.setStyle(defaultStyle);
        passwordField.setStyle(defaultStyle);
        confirmPasswordField.setStyle(defaultStyle);
        addressField.setStyle(defaultStyle);
        errorLabel.setText("");
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