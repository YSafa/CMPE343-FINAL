package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Alertutil {

    /**
     * Genel bir bilgi veya uyarı mesajı gösterir.
     * @param title Pencere başlığı
     * @param header Başlık metni (null olabilir)
     * @param content Mesaj içeriği
     * @param type Alert türü (INFORMATION, WARNING, ERROR, vb.)
     */
    public static void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays a critical error alert.
     * Used for unexpected or system-level errors.
     *
     * @param message Error message to display.
     */
    public static void showErrorMessage(String message) {
        showAlert("Error", "An error occurred", message, AlertType.ERROR);
    }

    /**
     * Displays a warning alert.
     * Used for user mistakes or invalid inputs.
     *
     * @param message Warning message to display.
     */
    public static void showWarningMessage(String message) {
        showAlert("Warning", "Attention Needed", message, AlertType.WARNING);
    }

    /**
     * Displays a success alert.
     * Used when an operation completes successfully.
     *
     * @param message Success message to display.
     */
    public static void showSuccessMessage(String message) {
        showAlert("Success", "Process Completed", message, AlertType.INFORMATION);
    }

    /**
     * Displays an informational alert.
     * Used to show general information or confirmation.
     *
     * @param message Informational message to display.
     */
    public static void showInfoMessage(String message) {
        showAlert("Information", null, message, AlertType.INFORMATION);
    }
}