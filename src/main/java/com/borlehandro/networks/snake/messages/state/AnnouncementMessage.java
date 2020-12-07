package com.borlehandro.networks.snake.messages.state;

import com.borlehandro.networks.snake.model.GameConfig;
import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.messages.MessageType;

import java.util.List;

// Should be sent by multicast
public class AnnouncementMessage extends StateMessage {
    private final List<Player> players;
    private final GameConfig gameConfig;

    public AnnouncementMessage(List<Player> players, GameConfig gameConfig, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.players = players;
        this.gameConfig = gameConfig;
        type = MessageType.ANNOUNCEMENT_MESSAGE;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public List<Player> getPlayers() {
        return players;
    }
}