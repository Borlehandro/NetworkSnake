package com.borlehandro.networks.snake.messages.factory;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton
 */
public class MessageFactory {

    private static MessageFactory instance;

    private GameStateMessageFactory gameStateMessageFactory;
    private AnnouncementMessageFactory announcementMessageFactory;

    private MessageFactory(){}

    public static MessageFactory getInstance() {
        if(instance == null)
            instance = new MessageFactory();
        return instance;
    }

    public void initGameStateMessageFactory(Map<Integer, Snake> snakeMap,
                                            SnakesProto.GameConfig gameConfig,
                                            Collection<Player> gamePlayers,
                                            Field field,
                                            AtomicInteger stateOrder) {
        gameStateMessageFactory = GameStateMessageFactory.getInstance(snakeMap,
                gameConfig,
                gamePlayers,
                field,
                stateOrder
        );
    }

    public void initAnnouncementMessageFactory(Collection<Player> players, SnakesProto.GameConfig config) {
        announcementMessageFactory = AnnouncementMessageFactory.getInstance(players, config);
    }

    public SnakesProto.GameMessage createAckMessage(long messageNumber, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                .build();
    }

    public SnakesProto.GameMessage createAnnouncementMessage(long messageNumber, int senderId, int receiverId) {
        return announcementMessageFactory.getMessage(messageNumber, senderId, receiverId);
    }

    public SnakesProto.GameMessage createErrorMessage(long messageNumber, int senderId, int receiverId, String errorMessage) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setError(SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(errorMessage).build())
                .build();
    }

    public SnakesProto.GameMessage createJoinMessage(long messageNumber, int senderId, int receiverId, String playerName) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder().setName(playerName).build())
                .build();
    }

    public SnakesProto.GameMessage createPingMessage(long messageNumber, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                .build();
    }

    public SnakesProto.GameMessage createRoleChangeMessage(long messageNumber,
                                                           int senderId,
                                                           int receiverId,
                                                           SnakesProto.NodeRole senderRole,
                                                           SnakesProto.NodeRole receiverRole) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                        .setSenderRole(senderRole).setReceiverRole(receiverRole).build())
                .build();
    }

    public SnakesProto.GameMessage createStateMessage(long messageNumber, int senderId, int receiverId) {
        return gameStateMessageFactory.getMessage(messageNumber, senderId, receiverId);
    }

    public SnakesProto.GameMessage createSteerMessage(long messageNumber, int senderId, int receiverId, Snake.Direction direction) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder().setDirection(switch (direction) {
                    case UP -> SnakesProto.Direction.UP;
                    case DOWN -> SnakesProto.Direction.DOWN;
                    case LEFT -> SnakesProto.Direction.LEFT;
                    case RIGHT -> SnakesProto.Direction.RIGHT;
                }).build())
                .build();
    }
}