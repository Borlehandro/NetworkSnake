package com.borlehandro.networks.snake.messages.action;

import com.borlehandro.networks.snake.messages.MessageType;

public class JoinMessage extends ActionMessage {
    final private String name;

    public JoinMessage(String name, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.name = name;
        type = MessageType.JOIN_MESSAGE;
    }

    public String getName() {
        return name;
    }
}
