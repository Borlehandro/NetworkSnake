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

    // public byte[] serialize();
}
