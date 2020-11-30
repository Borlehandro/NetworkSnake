package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.session.Session;
import com.borlehandro.networks.snake.protocol.GameConfig;

import java.util.Map;

public class OfflineMonitor extends Thread {
    private final Session session;
    private final int nodeTimeoutMillis;
    private final int pingDelayMillis;
    private final Map<Integer, Long> lastMessageMillis;

    public OfflineMonitor(Session session, GameConfig gameConfig, Map<Integer, Long> lastMessageMillis) {
        this.session = session;
        this.nodeTimeoutMillis = gameConfig.getNodeTimeoutMillis();
        this.pingDelayMillis = gameConfig.getPingDelayMillis();
        this.lastMessageMillis = lastMessageMillis;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            synchronized (lastMessageMillis) {
                Map.copyOf(lastMessageMillis).entrySet().stream()
                        .filter((entry) -> {
                            long delta = System.currentTimeMillis() - entry.getValue();
                            return delta > nodeTimeoutMillis;
                        })
                        .forEach((entry) -> session.onNodeOffline(entry.getKey()));
            }
            // Todo test
            try {
                sleep(pingDelayMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
