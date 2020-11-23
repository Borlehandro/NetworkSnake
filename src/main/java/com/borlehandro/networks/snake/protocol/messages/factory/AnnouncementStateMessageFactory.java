package com.borlehandro.networks.snake.protocol.messages.factory;

import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.messages.state.AnnouncementMessage;
import com.borlehandro.networks.snake.protocol.messages.state.StateMessage;

import java.util.List;
import java.util.Optional;

/**
 * Singleton
 */
public class AnnouncementStateMessageFactory implements StateMessageFactory {
    private final List<Player> players;
    private final GameConfig config;
    private static AnnouncementStateMessageFactory INSTANCE;
    private AnnouncementStateMessageFactory(List<Player> players, GameConfig config) {
        this.players = players;
        this.config = config;
    }

    public static Optional<AnnouncementStateMessageFactory> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static AnnouncementStateMessageFactory getInstance(List<Player> players, GameConfig config) {
        if(INSTANCE == null)
            INSTANCE = new AnnouncementStateMessageFactory(players, config);
        return INSTANCE;
    }

    @Override
    public StateMessage getMessage(long messageNumber, int senderId, int receiverId) {
        return new AnnouncementMessage(players, config, messageNumber, senderId, receiverId);
    }
}