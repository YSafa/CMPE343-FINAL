package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Utility class for showing alert messages.
 * It is used for error, warning, success, and confirmation alerts.
 */
public class Alertutil {

    /**
     * Shows a basic alert dialog.
     *
     * @param title alert title
     * @param header alert header text
     * @param content alert content text
     * @param type alert type
     */
    public static void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows an error message alert.
     *
     * @param message error message
     */
    public static void showErrorMessage(String message) {
        showAlert("Error", "An error occurred", message, AlertType.ERROR);
    }

    /**
     * Shows a warning message alert.
     *
     * @param message warning message
     */
    public static void showWarningMessage(String message) {
        showAlert("Warning", "Attention Needed", message, AlertType.WARNING);
    }

    /**
     * Shows a success message alert.
     *
     * @param message success message
     */
    public static void showSuccessMessage(String message) {
        showAlert("Success", "Process Completed", message, AlertType.INFORMATION);
    }

    /**
     * Shows an information message alert.
     *
     * @param message information message
     */

    public static void showInfoMessage(String message) {
        showAlert("Information", null, message, AlertType.INFORMATION);
    }

    /**
     * Shows a confirmation dialog with default header.
     *
     * @param title dialog title
     * @param content dialog message
     * @return true if user clicks Yes
     */
    public static boolean showConfirmation(String title, String content) {
        return showConfirmation(title, null, content);
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param title dialog title
     * @param header dialog header
     * @param content dialog message
     * @return true if user clicks Yes
     */
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