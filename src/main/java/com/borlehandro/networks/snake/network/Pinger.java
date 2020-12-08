package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.messages.factory.MessageFactory;
import com.borlehandro.networks.snake.model.SendTask;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.SocketAddress;
import java.util.Map;
import java.util.function.Function;

/**
 * Send messages to MASTER one time per pingDelayMillis
 */
public class Pinger extends Thread {
    private final int pingDelayMillis;
    private final NetworkActionsManager manager;
    private final int senderId;
    private final Map<Integer, Long> lastSentMessageTime;
    private final Function<Integer, SocketAddress> getAddressFunction;

    // TODO Refactor all this code
    public Pinger(SnakesProto.GameConfig config, NetworkActionsManager manager, int senderId, Map<Integer, Long> lastSentMessageTime, Function<Integer, SocketAddress> getAddressFunction) {
        pingDelayMillis = config.getPingDelayMs();
        this.manager = manager;
        this.senderId = senderId;
        this.lastSentMessageTime = lastSentMessageTime;
        this.getAddressFunction = getAddressFunction;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            // Todo test synchronized
            synchronized (manager) {
                synchronized (lastSentMessageTime) {
                    lastSentMessageTime.forEach((key, value) -> {
                                if (System.currentTimeMillis() - value > pingDelayMillis) {
                                    // System.err.println("I send ping from " + senderId + " to " + getAddressFunction.apply(key));
                                    // Todo Test ping fix
                                    manager.putSendTask(
                                            new SendTask(
                                                    MessageFactory.getInstance().createPingMessage(MessagesCounter.next(), senderId, key),
                                                    getAddressFunction.apply(key)));
                                }
                            }
                    );
                }
            }
            try {
                sleep(pingDelayMillis);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
