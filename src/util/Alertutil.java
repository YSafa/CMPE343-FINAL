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
     * Kritik hatalar için (örn: Veritabanı bağlantı hatası)
     */
    public static void showErrorMessage(String message) {
        showAlert("Error", "An error occurred", message, AlertType.ERROR);
    }

    /**
     * Kullanıcı hataları için (örn: Stok yetersiz, Geçersiz miktar)
     */
    public static void showWarningMessage(String message) {
        showAlert("Warning", "Attention Needed", message, AlertType.WARNING);
    }

    /**
     * Başarılı işlemler için (örn: Sipariş tamamlandı, Kayıt başarılı)
     */
    public static void showSuccessMessage(String message) {
        showAlert("Success", "Process Completed", message, AlertType.INFORMATION);
    }
}