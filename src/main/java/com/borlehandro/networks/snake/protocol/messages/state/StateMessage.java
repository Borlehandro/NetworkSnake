package com.borlehandro.networks.snake.protocol.messages.state;

import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.protocol.messages.Message;

/**
 * Marker class
 * All StateMessages should be created by StateMessageFactory
 */
public abstract class StateMessage extends Message {
    protected StateMessage(long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
    }
}
