package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.game.session.ClientSession;
import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.messages.Message;
import com.borlehandro.networks.snake.messages.action.RoleChangeMessage;
import com.borlehandro.networks.snake.messages.state.AnnouncementMessage;
import com.borlehandro.networks.snake.messages.state.GameStateMessage;

import java.net.InetSocketAddress;

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
                    Message message = task.getMessage();
                    InetSocketAddress socketAddress = task.getSocketAddress();
                    switch (message.getType()) {
                        case ANNOUNCEMENT_MESSAGE -> {
                            var msg = (AnnouncementMessage) message;
                            session.newAnnouncement(new ServerItem(msg.getGameConfig(), msg.getPlayers(), socketAddress));
                        }
                        case ACK_MESSAGE -> {
                            // Todo add normal handling
                            // Todo fix wrong calls!
                            // System.err.println("ACK");
                            session.acceptServer(message.getReceiverId());
                        }
                        case GAME_STATE_MESSAGE -> {
                            // Field update
                            var stateMessage = (GameStateMessage) message;
                            session.updateState(
                                    stateMessage.getStateOrder(),
                                    stateMessage.getSnakes(),
                                    stateMessage.getFoodCoordinates(),
                                    stateMessage.getPlayers(),
                                    stateMessage.getConfig()
                            );
                        }
                        case ROLE_CHANGE_MESSAGE -> {
                            var roleMessage = (RoleChangeMessage) message;
                            // Todo fix for another roles changing.
                            //  Use socket address.
                            session.onRolesChanged(roleMessage.getSenderRole(),
                                    roleMessage.getReceiverRole(),
                                    roleMessage.getSenderId());
                        }
                        case PING_MESSAGE -> {
                            // System.err.println("PING");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleMessage(Message message, InetSocketAddress socketAddress) {
        // Todo test
        System.err.println("UPDATE RECEIVED for " + message.getSenderId() + " to " + System.currentTimeMillis());
        serversRepository.updateLastReceivedMessageTimeMillis(message.getSenderId(), System.currentTimeMillis(), false);
        synchronized (tasksToHandle) {
            tasksToHandle.addLast(new HandleTask(message, socketAddress));
        }
    }
}
