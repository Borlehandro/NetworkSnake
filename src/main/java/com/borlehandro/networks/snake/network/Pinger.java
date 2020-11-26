package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.messages.action.PingMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Send messages to MASTER one time per pingDelayMillis
 */
public class Pinger {
    private final int pingDelayMillis;
    private final NetworkActionsManager manager;
    private final ExecutorService pingExecutor = Executors.newSingleThreadExecutor();
    private final int senderId;
    public Pinger(GameConfig config, NetworkActionsManager manager, int senderId) {
        pingDelayMillis = config.getPingDelayMillis();
        this.manager = manager;
        this.senderId = senderId;
    }

    public void start() {
        pingExecutor.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(pingDelayMillis);
                } catch (InterruptedException e) {
                    break;
                }
                int masterId = manager.getMasterId();
                if(masterId > 0) {
                    manager.putMessage(new PingMessage(MessagesCounter.next(), senderId, masterId));
                }
            }
        });
    }

}
