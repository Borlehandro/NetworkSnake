package com.borlehandro.networks.snake.protocol.messages.factory;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protocol.Coordinates;
import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.messages.state.GameStateMessage;
import com.borlehandro.networks.snake.protocol.messages.state.StateMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Be careful it's Singleton
 */
public class GameStateMessageFactory implements StateMessageFactory {
    // In-game entities
    private final Map<Integer, Snake> snakeMap;
    private final GameConfig gameConfig;
    private final Collection<Player> players;
    private final Field field;
    private final AtomicInteger stateOrder;


    private static GameStateMessageFactory INSTANCE;

    public static Optional<GameStateMessageFactory> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static GameStateMessageFactory getInstance(Map<Integer, Snake> snakeMap,
                                                      GameConfig gameConfig,
                                                      Collection<Player> players,
                                                      Field field,
                                                      AtomicInteger stateOrder) {
        if (INSTANCE == null) {
            INSTANCE = new GameStateMessageFactory(snakeMap, gameConfig, players, field, stateOrder);
        }
        return INSTANCE;
    }

    private GameStateMessageFactory(Map<Integer, Snake> snakeMap,
                                    GameConfig gameConfig,
                                    Collection<Player> players,
                                    Field field, AtomicInteger stateOrder) {
        this.snakeMap = snakeMap;
        this.gameConfig = gameConfig;
        this.players = players;
        this.field = field;
        this.stateOrder = stateOrder;
    }

    @Override
    public StateMessage getMessage(long messageNumber, int senderId, int receiverId) {
        var snakes = List.copyOf(snakeMap.values());
        List<FieldNode> fieldMatrix = new ArrayList<>();
        for (FieldNode[] row : field.getFieldMatrix()) {
            fieldMatrix.addAll(Arrays.asList(row));
        }
        var foodCoordinates = fieldMatrix
                .stream()
                .filter((fieldNode -> fieldNode.getState().equals(FieldNode.State.WITH_FOOD)))
                .map(fieldNode -> new Coordinates(fieldNode.getX(), fieldNode.getY()))
                .collect(Collectors.toList());
        return new GameStateMessage(stateOrder.get(), snakes, foodCoordinates, players, gameConfig, messageNumber, senderId, receiverId);
    }

}
