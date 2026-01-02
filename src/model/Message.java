package model;

import java.time.LocalDateTime;

/**
 * Represents a message between two users.
 * It stores sender, receiver, content, and time.
 */
public class Message {

    private int id;
    private int senderId;
    private int receiverId;
    private String message;
    private LocalDateTime sentAt;
    private boolean read;

    /**
     * Creates a message object.
     *
     * @param id message ID
     * @param senderId sender user ID
     * @param receiverId receiver user ID
     * @param message message text
     * @param sentAt send time
     * @param read read status
     */
    public Message(int id, int senderId, int receiverId, String message, LocalDateTime sentAt, boolean read) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.sentAt = sentAt;
        this.read = read;
    }

    /**
     * Gets the sender ID.
     *
     * @return sender ID
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Gets the receiver ID.
     *
     * @return receiver ID
     */
    public int getReceiverId() {
        return receiverId;
    }

    /**
     * Gets the message text.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the send time.
     *
     * @return send time
     */
    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
