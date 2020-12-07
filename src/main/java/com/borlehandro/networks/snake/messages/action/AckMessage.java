package com.borlehandro.networks.snake.messages.action;

import com.borlehandro.networks.snake.messages.MessageType;

public class AckMessage extends ActionMessage {
    public AckMessage(long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        type = MessageType.ACK_MESSAGE;
    }
}
