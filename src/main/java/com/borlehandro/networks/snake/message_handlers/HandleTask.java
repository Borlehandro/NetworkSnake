package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.messages.Message;

import java.net.InetSocketAddress;

// Public - debug only
public class HandleTask {
    private final Message message;
    private final InetSocketAddress socketAddress;

    HandleTask(Message message, InetSocketAddress socketAddress) {
        this.message = message;
        this.socketAddress = socketAddress;
    }

    public Message getMessage() {
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
