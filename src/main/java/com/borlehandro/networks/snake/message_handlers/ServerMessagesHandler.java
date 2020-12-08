package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.game.session.ServerSession;
import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.InetSocketAddress;

// TODO Refactor
public class ServerMessagesHandler extends MessagesHandler {
    private final ServerSession session;
    private final PlayersServersRepository playersRepository = PlayersServersRepository.getInstance();

    public ServerMessagesHandler(ServerSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            synchronized (tasksToHandle) {
                if (!tasksToHandle.isEmpty()) {
                    var task = tasksToHandle.pollFirst();
                    SnakesProto.GameMessage message = task.getMessage();
                    InetSocketAddress socketAddress = task.getSocketAddress();
                    switch (message.getTypeCase()) {
                        case STEER -> {
                            SnakesProto.GameMessage.SteerMsg m = message.getSteer();
                            System.err.println("Network rotate: " + message.getSenderId() + " to " + m.getDirection());
                            session.rotate(message.getSenderId(),
                                    switch (m.getDirection()) {
                                        case UP -> Snake.Direction.UP;
                                        case DOWN -> Snake.Direction.DOWN;
                                        case LEFT -> Snake.Direction.LEFT;
                                        case RIGHT -> Snake.Direction.RIGHT;
                                    });
                        }
                        case JOIN -> {
                            SnakesProto.GameMessage.JoinMsg m = message.getJoin();
                            System.err.println("JOIN " + System.currentTimeMillis());
                            Player player = Player
                                    .builder()
                                    .withName(m.getName())
                                    .withIpAddress(socketAddress.getHostString())
                                    .withPort(socketAddress.getPort())
                                    .build()
                                    .get();
                            long messageNumber = message.getMsgSeq();
                            // ...
                            synchronized (session) {
                                // System.err.println("JOIN get session monitor" + System.currentTimeMillis());
                                session.addPlayer(player, messageNumber);
                            }
                            // System.err.println("JOIN END " + System.currentTimeMillis());
                        }
                        case ROLE_CHANGE -> {
                            SnakesProto.GameMessage.RoleChangeMsg roleChangeMessage = message.getRoleChange();
                            if (roleChangeMessage.getSenderRole().equals(SnakesProto.NodeRole.VIEWER)) {
                                session.setViewer(message.getSenderId());
                            }
                            // Todo handle another role changes
                        }
                        case PING -> {
                            // System.err.println("PING: " + playersRepository.getLastReceivedMessageTimeMillis().get(message.getSenderId()));
                        }
                        case ACK -> {
                            // System.err.println("ACK: " +  playersRepository.getLastReceivedMessageTimeMillis().get(message.getSenderId()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleMessage(SnakesProto.GameMessage message, InetSocketAddress socketAddress) {
        synchronized (tasksToHandle) {
            tasksToHandle.addLast(new HandleTask(message, socketAddress));
        }
        playersRepository.updateLastReceivedMessageTimeMillis(message.getSenderId(), System.currentTimeMillis(), false);
    }
}
