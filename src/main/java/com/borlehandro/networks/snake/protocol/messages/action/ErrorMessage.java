package com.borlehandro.networks.snake.protocol.messages.action;

import com.borlehandro.networks.snake.protocol.messages.MessageType;
import com.borlehandro.networks.snake.protocol.messages.action.ActionMessage;

public class ErrorMessage extends ActionMessage {
    private final String errorMessage;


    public ErrorMessage(String errorMessage, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.errorMessage = errorMessage;
        type = MessageType.ERROR_MESSAGE;
    }
}
