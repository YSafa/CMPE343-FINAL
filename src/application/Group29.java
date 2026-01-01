package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import util.Alertutil;

import java.util.Optional;

public class Group29 extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 960, 540);

            stage.setTitle("Group29 GreenGrocer");
            stage.setScene(scene);

            // Uygulama kapanırken onay sorma işlemi
            stage.setOnCloseRequest(event -> {
                // Varsayılan kapatma işlemini durdur (Kullanıcı "Hayır" derse kapanmasın diye)
                event.consume(); 
                
                showExitConfirmation(stage);
            });

            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showExitConfirmation(Stage stage) {
    boolean confirmed = Alertutil.showConfirmation(
        "Exit Application",
        null,
        "Are you sure you want to close the application?"
    );

    if (confirmed) {
        stage.close();
    }
}

    public static void main(String[] args) {
        launch(args);
    }
}