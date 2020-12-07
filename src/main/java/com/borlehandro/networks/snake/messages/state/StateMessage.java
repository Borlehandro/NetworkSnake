package com.borlehandro.networks.snake.messages.state;

import com.borlehandro.networks.snake.messages.Message;

/**
 * Marker class
 * All StateMessages should be created by StateMessageFactory
 */
public abstract class StateMessage extends Message {
    protected StateMessage(long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
    }
}
