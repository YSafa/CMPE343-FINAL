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


public class MessageController {

    @FXML private ListView<String> messageList;
    @FXML private TextField messageField;

    private User currentUser;
    private User partner;
    private final MessageDAO dao = new MessageDAO();

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

                setText(null); // 🔥 çok önemli
                setGraphic(container);
                setStyle("-fx-background-color: transparent;");
            }
        });

        // Enter ile gönderme
        messageField.setOnAction(e -> handleSend());
    }


    public void setUsers(User currentUser, User partner) {
        this.currentUser = currentUser;
        this.partner = partner;
        loadMessages();
    }

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


    @FXML
    private void handleSend() {
        if (messageField.getText().isBlank()) return;
        dao.sendMessage(currentUser.getId(), partner.getId(), messageField.getText());
        messageField.clear();
        loadMessages();
    }
}
