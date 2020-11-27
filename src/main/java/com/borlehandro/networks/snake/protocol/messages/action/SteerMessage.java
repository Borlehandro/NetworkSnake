package com.borlehandro.networks.snake.protocol.messages.action;

import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protocol.messages.MessageType;

public class SteerMessage extends ActionMessage {
    private final Snake.Direction direction;
    public SteerMessage(Snake.Direction direction, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.direction = direction;
        type = MessageType.STEER_MESSAGE;
    }

    public Snake.Direction getDirection() {
        return direction;
    }
}