package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class Alertutil {

    public static void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showErrorMessage(String message) {
        showAlert("Error", "An error occurred", message, AlertType.ERROR);
    }

    public static void showWarningMessage(String message) {
        showAlert("Warning", "Attention Needed", message, AlertType.WARNING);
    }

    public static void showSuccessMessage(String message) {
        showAlert("Success", "Process Completed", message, AlertType.INFORMATION);
    }

    public static void showInfoMessage(String message) {
        showAlert("Information", null, message, AlertType.INFORMATION);
    }

    public static boolean showConfirmation(String title, String content) {
        return showConfirmation(title, null, content);
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType btnYes = new ButtonType("Yes");
        ButtonType btnNo = new ButtonType("No");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }
}