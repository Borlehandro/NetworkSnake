package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.game.session.ServerSession;
import com.borlehandro.networks.snake.protocol.NodeRole;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.action.JoinMessage;
import com.borlehandro.networks.snake.protocol.messages.action.RoleChangeMessage;
import com.borlehandro.networks.snake.protocol.messages.action.SteerMessage;

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
                    Message message = task.getMessage();
                    InetSocketAddress socketAddress = task.getSocketAddress();
                    switch (message.getType()) {
                        case STEER_MESSAGE -> {
                            SteerMessage m = (SteerMessage) message;
                            System.err.println("Network rotate: " + m.getSenderId() + " to " + m.getDirection());
                            session.rotate(m.getSenderId(), m.getDirection());
                        }
                        case JOIN_MESSAGE -> {
                            JoinMessage m = (JoinMessage) message;
                            System.err.println("JOIN");
                            session.addPlayer(Player
                                            .builder()
                                            .withName(m.getName())
                                            .withIpAddress(socketAddress.getHostName())
                                            .withPort(socketAddress.getPort())
                                            .build()
                                            .get(),
                                    m.getMessageNumber());
                        }
                        case ROLE_CHANGE_MESSAGE -> {
                            RoleChangeMessage roleChangeMessage = (RoleChangeMessage) message;
                            if(roleChangeMessage.getSenderRole().equals(NodeRole.VIEWER)) {
                                session.setViewer(message.getSenderId());
                            }
                            // Todo handle another role changes
                        }
                        case PING_MESSAGE -> {
                            System.err.println("PING: " + playersRepository.getLastReceivedMessageTimeMillis().get(message.getSenderId()));
                        }
                        case ACK_MESSAGE -> {
                            System.err.println("ACK: " +  playersRepository.getLastReceivedMessageTimeMillis().get(message.getSenderId()));
                        }
                    }
                }
            }
        }
    }

    public void handleMessage(Message message, InetSocketAddress socketAddress) {
        synchronized (tasksToHandle) {
            tasksToHandle.addLast(new HandleTask(message, socketAddress));
        }
        playersRepository.updateLastReceivedMessageTimeMillis(message.getSenderId(), System.currentTimeMillis(), false);
    }
}
