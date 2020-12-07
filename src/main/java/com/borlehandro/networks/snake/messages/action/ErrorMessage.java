package com.borlehandro.networks.snake.messages.action;

import com.borlehandro.networks.snake.messages.MessageType;

public class ErrorMessage extends ActionMessage {
    private final String errorMessage;


    public ErrorMessage(String errorMessage, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.errorMessage = errorMessage;
        type = MessageType.ERROR_MESSAGE;
    }
}
