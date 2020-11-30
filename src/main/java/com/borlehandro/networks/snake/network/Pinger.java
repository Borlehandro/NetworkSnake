package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.SendTask;
import com.borlehandro.networks.snake.protocol.messages.action.PingMessage;

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
    public Pinger(GameConfig config, NetworkActionsManager manager, int senderId, Map<Integer, Long> lastSentMessageTime, Function<Integer, SocketAddress> getAddressFunction) {
        pingDelayMillis = config.getPingDelayMillis();
        this.manager = manager;
        this.senderId = senderId;
        this.lastSentMessageTime = lastSentMessageTime;
        this.getAddressFunction = getAddressFunction;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            synchronized (lastSentMessageTime) {
                lastSentMessageTime.forEach((key, value) -> {
                            if (System.currentTimeMillis() - value > pingDelayMillis) {
                                System.err.println("I send ping from " + senderId + " to " + getAddressFunction.apply(key));
                                manager.putSendTask(
                                        new SendTask(
                                                new PingMessage(MessagesCounter.next(), senderId, 0),
                                                getAddressFunction.apply(key)));
                            }
                        }
                );
            }
            try {
                sleep(pingDelayMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
