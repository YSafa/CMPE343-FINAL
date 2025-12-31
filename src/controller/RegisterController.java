package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.Alertutil;
import model.User;


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

        String username = usernameField.getText();
        String password = passwordField.getText();
        String address  = addressField.getText();

        // 1️⃣ Boş alan kontrolü
        if (username.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Alertutil.showAlert(
                    "Error",
                    null,
                    "All fields are required.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        try {

            User user = new User(
                    0,              // id (AUTO_INCREMENT olduğu için)
                    username,
                    password,
                    "customer",     // role
                    address
            );

            boolean success = userDAO.registerCustomer(user);


            if (success) {
                Alertutil.showAlert(
                        "Success",
                        null,
                        "Registration successful. You can now log in.",
                        Alert.AlertType.INFORMATION
                );
                goBackToLogin();
            }

        }catch (SQLException e) {

            String msg = e.getMessage();

            if (msg != null && msg.toLowerCase().contains("username")) {
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
            Stage stage =
                    (Stage) usernameField.getScene().getWindow();

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));

            stage.setScene(new Scene(loader.load()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
