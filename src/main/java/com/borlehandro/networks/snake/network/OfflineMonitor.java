package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.api.Session;
import com.borlehandro.networks.snake.model.GameConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OfflineMonitor extends Thread {
    private final Session session;
    private final int nodeTimeoutMillis;
    private final int pingDelayMillis;
    private final Map<Integer, Long> lastReceivedMessageMillis;

    public OfflineMonitor(Session session, GameConfig gameConfig, Map<Integer, Long> lastReceivedMessageMillis) {
        this.session = session;
        this.nodeTimeoutMillis = gameConfig.getNodeTimeoutMillis();
        this.pingDelayMillis = gameConfig.getPingDelayMillis();
        this.lastReceivedMessageMillis = lastReceivedMessageMillis;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            List<Map.Entry<Integer, Long>> offlineUsers;
            synchronized (lastReceivedMessageMillis) {
                offlineUsers = lastReceivedMessageMillis.entrySet().stream()
                        .filter((entry) -> {
                            long delta = System.currentTimeMillis() - entry.getValue();
                            return delta > nodeTimeoutMillis;
                        }).collect(Collectors.toList());
            }
            // Todo test without sync
            offlineUsers.forEach((entry) -> session.onNodeOffline(entry.getKey()));
            try {
                sleep(pingDelayMillis);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
