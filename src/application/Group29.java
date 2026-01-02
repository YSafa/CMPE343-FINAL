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


/**
 * This class is the main application class.
 * It starts the JavaFX application.
 * The program runs from this class.
 */
public class Group29 extends Application {
    /**
     * This method starts the JavaFX application.
     * It loads the Login.fxml file and shows the main window.
     *
     * @param stage the main stage of the application
     */
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

    /**
     * This method shows a confirmation alert before closing the application.
     * If the user confirms, the application will close.
     *
     * @param stage the main stage of the application
     */
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

    /**
     * The main method of the program.
     * It launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}