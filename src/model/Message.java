package model;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int senderId;
    private int receiverId;
    private String message;
    private LocalDateTime sentAt;
    private boolean read;

    public Message(int id, int senderId, int receiverId, String message, LocalDateTime sentAt, boolean read) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.sentAt = sentAt;
        this.read = read;
    }

    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public LocalDateTime getSentAt() { return sentAt; }
}
