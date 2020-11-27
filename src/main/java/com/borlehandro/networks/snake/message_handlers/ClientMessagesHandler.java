package com.borlehandro.networks.snake.message_handlers;

import com.borlehandro.networks.snake.game.session.ClientSession;
import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.state.AnnouncementMessage;

import java.net.InetSocketAddress;

public class ClientMessagesHandler implements MessagesHandler {

    private final ClientSession session;

    public ClientMessagesHandler(ClientSession session) {
        this.session = session;
    }

    @Override
    public void handleMessage(Message message, InetSocketAddress socketAddress) {
        switch (message.getType()) {
            case ANNOUNCEMENT_MESSAGE -> {
                var msg = (AnnouncementMessage) message;
                session.newAnnouncement(new ServerItem(msg.getGameConfig(), msg.getPlayers(), socketAddress));
            }
            case ACK_MESSAGE -> {
                // Todo add normal handling
                System.out.println("Player joined " + message.getReceiverId());
                session.setMyId(message.getReceiverId());
                session.acceptServer();
            }
        }
    }
}
