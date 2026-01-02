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

/**
 * Controller for the register screen.
 * It creates a new customer account after validation.
 */
public class RegisterController {

    /** Username input field. */
    @FXML private TextField usernameField;

    /** Password input field. */
    @FXML private PasswordField passwordField;

    /** Confirm password input field. */
    @FXML private PasswordField confirmPasswordField;

    /** Address input field. */
    @FXML private TextField addressField;

    /** Label for showing errors. */
    @FXML private Label errorLabel;

    /** DAO for user database actions. */
    private final UserDAO userDAO = new UserDAO();

    /** Default style for input fields. */
    private final String defaultStyle =
            "-fx-border-color: #dcdcdc; -fx-border-radius: 10; -fx-background-radius: 10;";

    /** Error style for input fields. */
    private final String errorStyle =
            "-fx-border-color: #ff4d4d; -fx-border-width: 2px; -fx-border-radius: 10; -fx-background-radius: 10;";

    /**
     * Called when the user clicks Register.
     * It validates inputs and saves the new customer in the database.
     */
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


        try {
            User user = new User(0, username, password , "customer", address);
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

    /**
     * Marks a field with error style.
     *
     * @param field the UI control to mark
     */
    private void markAsError(Control field) {
        field.setStyle(errorStyle);
    }

    /**
     * Resets styles and clears the error message.
     */
    private void resetStyles() {
        usernameField.setStyle(defaultStyle);
        passwordField.setStyle(defaultStyle);
        confirmPasswordField.setStyle(defaultStyle);
        addressField.setStyle(defaultStyle);
        errorLabel.setText("");
    }

    /**
     * Called when user clicks Back.
     * It returns to the login screen.
     */
    @FXML
    private void handleBack() {
        goBackToLogin();
    }

    /**
     * Loads the login screen (Login.fxml).
     */
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