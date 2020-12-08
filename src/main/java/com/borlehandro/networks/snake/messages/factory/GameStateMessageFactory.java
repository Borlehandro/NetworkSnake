package com.borlehandro.networks.snake.messages.factory;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Be careful it's Singleton
 */
public class GameStateMessageFactory {
    // In-game entities
    private final Map<Integer, Snake> snakeMap;
    private final SnakesProto.GameConfig gameConfig;
    private final Collection<Player> players;
    private final Field field;
    private final AtomicInteger stateOrder;


    private static GameStateMessageFactory INSTANCE;

    public static Optional<GameStateMessageFactory> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static GameStateMessageFactory getInstance(Map<Integer, Snake> snakeMap,
                                                      SnakesProto.GameConfig gameConfig,
                                                      Collection<Player> players,
                                                      Field field,
                                                      AtomicInteger stateOrder) {
        if (INSTANCE == null) {
            INSTANCE = new GameStateMessageFactory(snakeMap, gameConfig, players, field, stateOrder);
        }
        return INSTANCE;
    }

    private GameStateMessageFactory(Map<Integer, Snake> snakeMap,
                                    SnakesProto.GameConfig gameConfig,
                                    Collection<Player> players,
                                    Field field, AtomicInteger stateOrder) {
        this.snakeMap = snakeMap;
        this.gameConfig = gameConfig;
        this.players = players;
        this.field = field;
        this.stateOrder = stateOrder;
    }

    public SnakesProto.GameMessage getMessage(long messageNumber, int senderId, int receiverId) {
        // Todo write distance to the points!
        var snakes = snakeMap.values().stream().map(
                snake -> SnakesProto.GameState.Snake.newBuilder()
                        .setState(switch (snake.getState()) {
                            case ALIVE -> SnakesProto.GameState.Snake.SnakeState.ALIVE;
                            case ZOMBIE -> SnakesProto.GameState.Snake.SnakeState.ZOMBIE;
                        })
                        .setPlayerId(snake.getPlayerId())
                        .addAllPoints(snake.getBody().stream().map(snakeNode -> SnakesProto.GameState.Coord.newBuilder()
                                .setX(snakeNode.getX())
                                .setY(snakeNode.getY())
                                .build())
                                .collect(Collectors.toList()))
                        .setHeadDirection(switch (snake.getBody().getFirst().getNodeDirection()) {
                            case UP -> SnakesProto.Direction.UP;
                            case DOWN -> SnakesProto.Direction.DOWN;
                            case LEFT -> SnakesProto.Direction.LEFT;
                            case RIGHT -> SnakesProto.Direction.RIGHT;
                        })
                        .build()
        ).collect(Collectors.toList());

        List<FieldNode> fieldMatrix = new ArrayList<>();

        for (FieldNode[] row : field.getFieldMatrix()) {
            fieldMatrix.addAll(Arrays.asList(row));
        }

        var foodCoordinates = fieldMatrix
                .stream()
                .filter((fieldNode -> fieldNode.getState().equals(FieldNode.State.WITH_FOOD)))
                .map(fieldNode -> SnakesProto.GameState.Coord.newBuilder().setX(fieldNode.getX()).setY(fieldNode.getY()).build())
                .collect(Collectors.toList());

        var gamePlayers = SnakesProto.GamePlayers.newBuilder()
                .addAllPlayers(players.stream().map(player ->
                        SnakesProto.GamePlayer.newBuilder()
                                .setName(player.getName())
                                .setId(player.getId())
                                .setIpAddress(player.getIpAddress())
                                .setPort(player.getPort())
                                .setScore(player.getScore())
                                .setRole(switch (player.getRole()) {
                                    case MASTER -> SnakesProto.NodeRole.MASTER;
                                    case DEPUTY -> SnakesProto.NodeRole.DEPUTY;
                                    case NORMAL -> SnakesProto.NodeRole.NORMAL;
                                    case VIEWER -> SnakesProto.NodeRole.VIEWER;
                                })
                                .setType(SnakesProto.PlayerType.HUMAN)
                                .build()).collect(Collectors.toList()))
                .build();

        return SnakesProto.GameMessage.newBuilder()
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setMsgSeq(messageNumber)
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder()
                        .setState(SnakesProto.GameState.newBuilder()
                                .addAllSnakes(snakes)
                                .addAllFoods(foodCoordinates)
                                .setPlayers(gamePlayers)
                                .setConfig(gameConfig)
                                .setStateOrder(stateOrder.get()))
                        .build())
                .build();
        // GameStateMessage(stateOrder.get(), snakes, foodCoordinates, players, gameConfig, messageNumber, senderId, receiverId);
    }
}