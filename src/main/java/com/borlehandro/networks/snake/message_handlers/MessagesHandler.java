package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.protocol.messages.Message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @contract All message handlers can only use Session classes to perform handling
 */
public interface MessagesHandler {
    void handleMessage(Message message, InetSocketAddress socketAddress);
}
