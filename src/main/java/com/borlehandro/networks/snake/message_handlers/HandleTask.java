package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.InetSocketAddress;

// Public - debug only
public class HandleTask {
    private final SnakesProto.GameMessage message;
    private final InetSocketAddress socketAddress;

    HandleTask(SnakesProto.GameMessage message, InetSocketAddress socketAddress) {
        this.message = message;
        this.socketAddress = socketAddress;
    }

    public SnakesProto.GameMessage getMessage() {
        return message;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    @Override
    public String toString() {
        return "HandleTask{" +
                "message=" + message.toString() +
                ", socketAddress=" + socketAddress.toString() +
                '}';
    }
}
