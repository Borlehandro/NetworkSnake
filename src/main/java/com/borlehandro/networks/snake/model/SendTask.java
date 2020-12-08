package com.borlehandro.networks.snake.model;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.SocketAddress;

public class SendTask {
    private final SnakesProto.GameMessage message;
    private final SocketAddress receiverAddress;

    public SendTask(SnakesProto.GameMessage message, SocketAddress receiverAddress) {
        this.message = message;
        this.receiverAddress = receiverAddress;
    }

    public SnakesProto.GameMessage getMessage() {
        return message;
    }

    public SocketAddress getReceiverAddress() {
        return receiverAddress;
    }
}
