package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.game.session.ServerSession;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.action.JoinMessage;
import com.borlehandro.networks.snake.protocol.messages.action.SteerMessage;

import java.net.InetSocketAddress;

// TODO Refactor
public class ServerMessagesHandler implements MessagesHandler {
    private final ServerSession session;

    public ServerMessagesHandler(ServerSession session) {
        this.session = session;
    }

    public void handleMessage(Message message, InetSocketAddress socket) {
        switch (message.getType()) {
            case STEER_MESSAGE -> {
                SteerMessage m = (SteerMessage) message;
                session.rotate(m.getSenderId(), m.getDirection());
            }
            case JOIN_MESSAGE -> {
                JoinMessage m = (JoinMessage) message;
                session.addPlayer(Player
                        .builder()
                        .withName(m.getName())
                        .withIpAddress(socket.getHostName())
                        .withPort(socket.getPort())
                        .build()
                        .get());
            }
        }
    }
}
