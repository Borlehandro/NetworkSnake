package com.borlehandro.networks.snake.protocol.messages.factory;

import com.borlehandro.networks.snake.protocol.messages.state.StateMessage;

/**
 * Convert in-game entities to message entities
 */
public interface StateMessageFactory {
    StateMessage getMessage(long messageNumber, int senderId, int receiverId);
}
