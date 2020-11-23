package com.borlehandro.networks.snake.protocol.messages.action;

import com.borlehandro.networks.snake.protocol.messages.MessageType;
import com.borlehandro.networks.snake.protocol.messages.action.ActionMessage;

public class JoinMessage extends ActionMessage {
    final private String name;

    public JoinMessage(String name, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.name = name;
        type = MessageType.JOIN_MESSAGE;
    }
}
