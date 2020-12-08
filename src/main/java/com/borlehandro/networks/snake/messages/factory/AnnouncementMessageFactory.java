package com.borlehandro.networks.snake.messages.factory;

import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Singleton
 */
public class AnnouncementMessageFactory {
    private final Collection<Player> players;
    private final SnakesProto.GameConfig config;
    private static AnnouncementMessageFactory INSTANCE;

    private AnnouncementMessageFactory(Collection<Player> players, SnakesProto.GameConfig config) {
        this.players = players;
        this.config = config;
    }

    public static Optional<AnnouncementMessageFactory> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static AnnouncementMessageFactory getInstance(Collection<Player> players, SnakesProto.GameConfig config) {
        if (INSTANCE == null)
            INSTANCE = new AnnouncementMessageFactory(players, config);
        return INSTANCE;
    }

    public SnakesProto.GameMessage getMessage(long messageNumber, int senderId, int receiverId) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageNumber)
                .setReceiverId(senderId)
                .setReceiverId(receiverId)
                .setAnnouncement(SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                        .setConfig(config)
                        .setPlayers(SnakesProto.GamePlayers.newBuilder()
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
                                .build())
                        .build())
                .build();
        // return new AnnouncementMessage(players, config, messageNumber, senderId, receiverId);
    }
}