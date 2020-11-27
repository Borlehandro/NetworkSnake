package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.PlayersServersRepository;
import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.messages.state.AnnouncementMessage;

/**
 * Should be launched by ServerSession or ServerLauncher
 */
public class MulticastClock extends Thread {

    // Todo load from game config
    private static final int MULTICAST_MESSAGES_PERIOD = 3000;
    private final NetworkActionsManager manager;
    private final GameConfig config;
    private final PlayersServersRepository playersServersRepository = PlayersServersRepository.getInstance();

    public MulticastClock(NetworkActionsManager manager, GameConfig config) {
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
            manager.putMulticast(new AnnouncementMessage(
                    playersServersRepository.getPlayersCopy(),
                    config, MessagesCounter.next(),
                    0,
                    0));
        }
    }

}
