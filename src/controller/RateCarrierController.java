package controller;

import dao.RatingDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import util.Alertutil;

/**
 * Controller for rating a carrier.
 * Customer can give a score and a comment.
 */
public class RateCarrierController {

    /** ComboBox for rating value (1–5). */
    @FXML private ComboBox<Integer> ratingBox;
    /** Text area for optional comment. */
    @FXML private TextArea commentArea;

    /** Customer ID. */
    private int customerId;

    /** Order ID. */
    private int orderId;

    /** Carrier ID. */
    private int carrierId;

    /** Action to run after successful rating. */
    private Runnable onSuccessRefresh;

    /**
     * Initializes required data for rating.
     *
     * @param customerId customer ID
     * @param orderId order ID
     * @param carrierId carrier ID
     * @param onSuccessRefresh action after success
     */
    public void init(int customerId,
                     int orderId,
                     int carrierId,
                     Runnable onSuccessRefresh) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.carrierId = carrierId;
        this.onSuccessRefresh = onSuccessRefresh;
    }

    /**
     * Runs when the view is loaded.
     * It fills rating values from 1 to 5.
     */
    @FXML
    public void initialize() {
        for (int i = 1; i <= 5; i++) {
            ratingBox.getItems().add(i);
        }
        ratingBox.getSelectionModel().select(5);
    }

    /**
     * Submits the rating and comment.
     * It saves data to the database.
     */
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

    /**
     * Closes the rating window.
     */
    @FXML
    private void handleClose() {
        Stage s =
                (Stage) ratingBox.getScene().getWindow();
        s.close();
    }
}
