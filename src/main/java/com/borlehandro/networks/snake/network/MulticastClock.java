package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.messages.factory.MessageFactory;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

/**
 * Should be launched by ServerSession or ServerLauncher
 */
public class MulticastClock extends Thread {

    // Todo load from game config
    private static final int MULTICAST_MESSAGES_PERIOD = 3000;
    private final NetworkActionsManager manager;
    private final SnakesProto.GameConfig config;
    private final PlayersServersRepository playersServersRepository = PlayersServersRepository.getInstance();

    public MulticastClock(NetworkActionsManager manager, SnakesProto.GameConfig config) {
        this.manager = manager;
        this.config = config;
    }

    public void run() {
        while (!interrupted()) {
            try {
                sleep(MULTICAST_MESSAGES_PERIOD);
            } catch (InterruptedException e) {
                return;
            }
            // Todo test
            manager.putMulticast(MessageFactory.getInstance().createAnnouncementMessage(MessagesCounter.next(), 0, 0));
        }
    }

}
