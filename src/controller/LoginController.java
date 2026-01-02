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


/**
 * This controller manages the login screen.
 * It checks username and password from the database.
 * If login is correct, it opens the correct dashboard by user role.
 */
public class LoginController {

    /** Text field for username input. */
    @FXML private TextField usernameField;

    /** Password field for password input. */
    @FXML private PasswordField passwordField;

    /** Main box of the login form (used for shake animation). */
    @FXML private VBox loginBox;

    /** Label used to show error messages to the user. */
    @FXML private Label errorLabel;

    /** Default style for input fields. */
    private final String defaultStyle =
            "-fx-border-color: #dcdcdc; -fx-border-radius: 10; -fx-background-radius: 10;";

    /** Error style for input fields when login fails. */
    private final String errorStyle =
            "-fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;";

    /**
     * Called when the user clicks the Login button.
     * It checks:
     * - fields are not empty
     * - user exists in database
     * - password hash matches the stored password
     * If login is successful, it opens the user dashboard.
     */
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
                String dbPassword = rs.getString("password");
                String inputHash = PasswordUtil.hashPassword(password);

                if (inputHash != null && inputHash.equals(dbPassword)) {
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


    /**
     * Shows an error message and adds a shake animation to the login box.
     * It also applies error style to username and password fields.
     *
     * @param message the error message to show
     */
    private void showError(String message) {
        errorLabel.setText(message);
        usernameField.setStyle(errorStyle);
        passwordField.setStyle(errorStyle);

        TranslateTransition tt = new TranslateTransition(Duration.millis(50), loginBox);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }


    /**
     * Resets error label and input field styles to default.
     * This is called before checking login.
     */
    private void resetStyles() {
        errorLabel.setText("");
        usernameField.setStyle(defaultStyle);
        passwordField.setStyle(defaultStyle);
    }


    /**
     * Loads the dashboard screen for the given user.
     * It selects an FXML file based on the user role:
     * customer, carrier, or owner.
     *
     * @param user the logged-in user
     * @throws Exception if FXML cannot be loaded
     */
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

    /**
     * Opens the register screen when the user clicks Register.
     * It loads Register.fxml and changes the scene.
     */
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
        }
    }
}