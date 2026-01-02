package controller;

import dao.MessageDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.Message;
import model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;


/**
 * This controller manages the messaging screen.
 * It shows messages between two users.
 * Users can send and receive text messages.
 */
public class MessageController {

    /** ListView that shows the message bubbles. */
    @FXML private ListView<String> messageList;

    /** Text field where the user writes a message. */
    @FXML private TextField messageField;

    /** The logged-in user. */
    private User currentUser;

    /** The other user in the conversation. */
    private User partner;

    /** DAO used to get and send messages from the database. */
    private final MessageDAO dao = new MessageDAO();

    /**
     * Initializes the message list UI.
     * It creates message bubbles and aligns them left or right.
     * It also allows sending message by pressing Enter.
     */
    @FXML
    private void initialize() {
        messageList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                Label bubble = new Label(msg);
                bubble.setWrapText(true);
                bubble.setMaxWidth(300);
                bubble.setPadding(new Insets(10));

                HBox container = new HBox(bubble);
                container.setPadding(new Insets(6));

                if (msg.startsWith("Me:")) {
                    bubble.setStyle(
                            "-fx-background-color: #dcf8c6;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-text-fill: #000000;" +
                                    "-fx-font-size: 13px;"
                    );
                    container.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    bubble.setStyle(
                            "-fx-background-color: #ffffff;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-text-fill: #000000;" +
                                    "-fx-font-size: 13px;"
                    );
                    container.setAlignment(Pos.CENTER_LEFT);
                }

                setText(null);
                setGraphic(container);
                setStyle("-fx-background-color: transparent;");
            }
        });

        // Enter ile gönderme
        messageField.setOnAction(e -> handleSend());
    }

    /**
     * Sets the users in the conversation.
     * It also loads previous messages.
     *
     * @param currentUser the logged-in user
     * @param partner the other user
     */
    public void setUsers(User currentUser, User partner) {
        this.currentUser = currentUser;
        this.partner = partner;
        loadMessages();
    }

    /**
     * Loads all messages between the two users.
     * Messages are shown as "Me" or "Them".
     * After loading, it scrolls to the last message.
     */
    private void loadMessages() {
        var msgs = dao.getConversation(currentUser.getId(), partner.getId());

        messageList.setItems(FXCollections.observableArrayList(
                msgs.stream()
                        .filter(m -> m.getMessage() != null)
                        .map(m ->
                                (m.getSenderId() == currentUser.getId() ? "Me: " : "Them: ")
                                        + m.getMessage()
                        )
                        .toList()
        ));

        if (!messageList.getItems().isEmpty()) {
            messageList.scrollTo(messageList.getItems().size() - 1);
        }
    }

    /**
     * Sends a new message to the other user.
     * It saves the message in the database and reloads the list.
     */
    @FXML
    private void handleSend() {
        if (messageField.getText().isBlank()) return;
        dao.sendMessage(currentUser.getId(), partner.getId(), messageField.getText());
        messageField.clear();
        loadMessages();
    }
}