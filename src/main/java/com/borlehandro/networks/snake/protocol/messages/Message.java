package com.borlehandro.networks.snake.protocol.messages;

// TODO Add serialization
public abstract class Message {
    private final long messageNumber;
    private final int senderId;
    private final int receiverId;
    protected MessageType type;

    protected Message(long messageNumber, int senderId, int receiverId) {
        this.messageNumber = messageNumber;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public long getMessageNumber() {
        return messageNumber;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public MessageType getType() {
        return type;
    }


    @Override
    public String toString() {
        return "Message{" +
                "messageNumber=" + messageNumber +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", type=" + type +
                '}';
    }
}
