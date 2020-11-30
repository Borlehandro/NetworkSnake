package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.SendTask;

import java.util.Map;

// Todo move into pinger
public class RepeatController extends Thread {
    private final Map<SendTask, Long> waitResponseMessages;
    private final int pingDelayMillis;
    private final NetworkActionsManager manager;
    public RepeatController(NetworkActionsManager manager, Map<SendTask, Long> waitResponseMessages, GameConfig config) {
        this.waitResponseMessages = waitResponseMessages;
        pingDelayMillis = config.getPingDelayMillis();
        this.manager = manager;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            synchronized (waitResponseMessages) {
                var iterator = waitResponseMessages.entrySet().iterator();
                while (iterator.hasNext()) {
                    var item = iterator.next();
                    SendTask sendTask = item.getKey();
                    long lastTime = item.getValue();
                    if(System.currentTimeMillis() - lastTime > pingDelayMillis) {
                        manager.putSendTask(sendTask);
                        iterator.remove();
                    }
                }
            }
        }
    }
}
