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
public class AnnouncementMessageFactory implements StateMessageFactory {
    private final List<Player> players;
    private final GameConfig config;
    private static AnnouncementMessageFactory INSTANCE;
    private AnnouncementMessageFactory(List<Player> players, GameConfig config) {
        this.players = players;
        this.config = config;
    }

    public static Optional<AnnouncementMessageFactory> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static AnnouncementMessageFactory getInstance(List<Player> players, GameConfig config) {
        if(INSTANCE == null)
            INSTANCE = new AnnouncementMessageFactory(players, config);
        return INSTANCE;
    }

    @Override
    public StateMessage getMessage(long messageNumber, int senderId, int receiverId) {
        return new AnnouncementMessage(players, config, messageNumber, senderId, receiverId);
    }
}