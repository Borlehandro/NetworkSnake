package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.model.SendTask;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.util.Map;

// Todo move into pinger
public class RepeatController extends Thread {
    private final Map<SendTask, Long> waitResponseMessages;
    private final int pingDelayMillis;
    private final NetworkActionsManager manager;
    private final Map<Integer, Long> lastSentMessagesTime;
    public RepeatController(NetworkActionsManager manager, Map<SendTask, Long> waitResponseMessages, SnakesProto.GameConfig config, Map<Integer, Long> lastSentMessagesTime) {
        this.waitResponseMessages = waitResponseMessages;
        pingDelayMillis = config.getPingDelayMs();
        this.manager = manager;
        this.lastSentMessagesTime = lastSentMessagesTime;
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
                    if(System.currentTimeMillis() - lastTime > pingDelayMillis
                            && lastSentMessagesTime.containsKey(sendTask.getMessage().getReceiverId())) {
                        manager.putSendTask(sendTask);
                        iterator.remove();
                    }
                }
            }
        }
    }
}
