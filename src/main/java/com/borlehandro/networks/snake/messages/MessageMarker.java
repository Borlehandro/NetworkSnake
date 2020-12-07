package com.borlehandro.networks.snake.messages;

public class MessageMarker {

    private MessageType type;

    public MessageMarker(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }
}
