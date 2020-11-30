package com.borlehandro.networks.snake.protocol.messages.state;

import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protocol.Coordinates;
import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.messages.MessageType;

import java.util.Collection;
import java.util.List;

public class GameStateMessage extends StateMessage {
    private final int stateOrder;
    private final List<Snake> snakes;
    private final List<Coordinates> foodCoordinates;
    private final Collection<Player> players;
    private final GameConfig config;

    public GameStateMessage(int stateOrder,
                            List<Snake> snakes,
                            List<Coordinates> foodCoordinates,
                            Collection<Player> players,
                            GameConfig config,
                            long messageNumber,
                            int senderId,
                            int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.stateOrder = stateOrder;
        this.snakes = snakes;
        this.foodCoordinates = foodCoordinates;
        this.players = players;
        this.config = config;
        type = MessageType.GAME_STATE_MESSAGE;
    }

    public int getStateOrder() {
        return stateOrder;
    }

    public List<Snake> getSnakes() {
        return snakes;
    }

    public List<Coordinates> getFoodCoordinates() {
        return foodCoordinates;
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public GameConfig getConfig() {
        return config;
    }
}
