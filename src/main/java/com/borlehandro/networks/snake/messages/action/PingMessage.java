package com.borlehandro.networks.snake.messages.action;

import com.borlehandro.networks.snake.messages.MessageType;

public class PingMessage extends ActionMessage {
    public PingMessage(long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        type = MessageType.PING_MESSAGE;
    }
}
