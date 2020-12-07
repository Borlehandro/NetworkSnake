package com.borlehandro.networks.snake.model;

import com.borlehandro.networks.snake.messages.Message;

import java.net.SocketAddress;

public class SendTask {
    private final Message message;
    private final SocketAddress receiverAddress;

    public SendTask(Message message, SocketAddress receiverAddress) {
        this.message = message;
        this.receiverAddress = receiverAddress;
    }

    public Message getMessage() {
        return message;
    }

    public SocketAddress getReceiverAddress() {
        return receiverAddress;
    }
}
