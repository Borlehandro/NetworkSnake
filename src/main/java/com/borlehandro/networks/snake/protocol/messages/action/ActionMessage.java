package com.borlehandro.networks.snake.protocol.messages.action;

import com.borlehandro.networks.snake.protocol.messages.Message;

public abstract class ActionMessage extends Message {

    protected ActionMessage(long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
    }
}
