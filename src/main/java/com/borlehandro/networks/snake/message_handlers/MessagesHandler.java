package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;

/**
 * @contract All message handlers can only use Session classes to perform handling
 */
public abstract class MessagesHandler extends Thread {
    protected final ArrayDeque<HandleTask> tasksToHandle = new ArrayDeque<>();
    abstract public void handleMessage(SnakesProto.GameMessage message, InetSocketAddress socketAddress);
}
