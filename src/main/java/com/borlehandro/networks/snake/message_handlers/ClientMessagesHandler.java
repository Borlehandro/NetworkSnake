package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.game.session.ClientSession;
import com.borlehandro.networks.snake.model.NodeRole;
import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.InetSocketAddress;
import java.util.stream.Collectors;

public class ClientMessagesHandler extends MessagesHandler {

    private final ClientSession session;
    private final PlayersServersRepository serversRepository = PlayersServersRepository.getInstance();

    public ClientMessagesHandler(ClientSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            synchronized (tasksToHandle) {
                if (!tasksToHandle.isEmpty()) {
                    // Debug
//                    System.err.println("--------------");
//                    tasksToHandle.forEach(System.err::println);
//                    System.err.println("--------------");
                    var task = tasksToHandle.pollFirst();
                    SnakesProto.GameMessage message = task.getMessage();
                    InetSocketAddress socketAddress = task.getSocketAddress();
                    switch (message.getTypeCase()) {
                        case ANNOUNCEMENT -> {
                            var msg = message.getAnnouncement();
                            session.newAnnouncement(new ServerItem(
                                    msg.getConfig(),
                                    msg.getPlayers().getPlayersList().stream()
                                            .map(Player::ofProtoPlayer)
                                            .collect(Collectors.toList()),
                                    socketAddress)
                            );
                        }
                        case ACK -> {
                            // Todo add normal handling
                            // Todo fix wrong calls!
                            // System.err.println("ACK");
                            session.acceptServer(message.getReceiverId());
                        }
                        case STATE -> {
                            // Field update
                            var stateMessage = message.getState().getState();
                            session.updateState(
                                    stateMessage.getStateOrder(),
                                    stateMessage.getSnakesList().stream()
                                            .map(Snake::ofProtoSnake)
                                            .collect(Collectors.toList()),
                                    stateMessage.getFoodsList(),
                                    stateMessage.getPlayers().getPlayersList().stream()
                                            .map(Player::ofProtoPlayer)
                                            .collect(Collectors.toList()),
                                    stateMessage.getConfig()
                            );
                        }
                        case ROLE_CHANGE -> {
                            var roleMessage = message.getRoleChange();
                            // Todo fix for another roles changing.
                            //  Use socket address.
                            session.onRolesChanged(
                                    switch (roleMessage.getSenderRole()) {
                                        case MASTER -> NodeRole.MASTER;
                                        case DEPUTY -> NodeRole.DEPUTY;
                                        case NORMAL -> NodeRole.NORMAL;
                                        case VIEWER -> NodeRole.VIEWER;
                                    },
                                    switch (roleMessage.getReceiverRole()) {
                                        case MASTER -> NodeRole.MASTER;
                                        case DEPUTY -> NodeRole.DEPUTY;
                                        case NORMAL -> NodeRole.NORMAL;
                                        case VIEWER -> NodeRole.VIEWER;
                                    },
                                    message.getSenderId());
                        }
                        case PING -> {
                            // System.err.println("PING");
                        }
                    }
                }
            }
        }

    }

    @Override
    public void handleMessage(SnakesProto.GameMessage message, InetSocketAddress socketAddress) {
        // Todo test
        System.err.println("UPDATE RECEIVED for " + message.getSenderId() + " to " + System.currentTimeMillis());
        serversRepository.updateLastReceivedMessageTimeMillis(message.getSenderId(), System.currentTimeMillis(), false);
        synchronized (tasksToHandle) {
            tasksToHandle.addLast(new HandleTask(message, socketAddress));
        }
    }
}
