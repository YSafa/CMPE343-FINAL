package controller;

import dao.RatingDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import util.Alertutil;

public class RateCarrierController {

    @FXML private ComboBox<Integer> ratingBox;
    @FXML private TextArea commentArea;

    private int customerId;
    private int orderId;
    private int carrierId;
    private Runnable onSuccessRefresh;

    public void init(int customerId,
                     int orderId,
                     int carrierId,
                     Runnable onSuccessRefresh) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.carrierId = carrierId;
        this.onSuccessRefresh = onSuccessRefresh;
    }

    @FXML
    public void initialize() {
        for (int i = 1; i <= 5; i++) {
            ratingBox.getItems().add(i);
        }
        ratingBox.getSelectionModel().select(5);
    }

    @FXML
    private void handleSubmit() {

        Integer rating = ratingBox.getValue();
        if (rating == null) {
            Alertutil.showWarningMessage("Select a rating.");
            return;
        }

        String comment = commentArea.getText();

        RatingDAO dao = new RatingDAO();
        boolean ok = dao.addRating(
                orderId,
                customerId,
                carrierId,
                rating,
                comment
        );

        if (ok) {
            Alertutil.showSuccessMessage(
                    "Thank you for your feedback!"
            );
            if (onSuccessRefresh != null)
                onSuccessRefresh.run();
            handleClose();
        } else {
            Alertutil.showErrorMessage(
                    "Rating could not be saved."
            );
        }
    }

    @FXML
    private void handleClose() {
        Stage s =
                (Stage) ratingBox.getScene().getWindow();
        s.close();
    }
}
