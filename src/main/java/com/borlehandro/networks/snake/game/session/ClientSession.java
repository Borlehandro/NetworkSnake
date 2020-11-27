package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.PlayersServersRepository;
import com.borlehandro.networks.snake.message_handlers.ClientMessagesHandler;
import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.network.AnnounceReceiver;
import com.borlehandro.networks.snake.network.MessagesCounter;
import com.borlehandro.networks.snake.network.NetworkActionsManager;
import com.borlehandro.networks.snake.protocol.SendTask;
import com.borlehandro.networks.snake.protocol.messages.action.JoinMessage;
import com.borlehandro.networks.snake.protocol.messages.action.SteerMessage;

import java.io.IOException;
import java.util.function.Consumer;

public class ClientSession {
    private Consumer<ServerItem> onNewAnnouncement;
    // Todo use normal structure
    // private Set<AnnouncementMessage> gamesToConnect = new HashSet<>();
    private ClientMessagesHandler messagesHandler;
    private NetworkActionsManager networkManager;
    private PlayersServersRepository serversRepository = PlayersServersRepository.getInstance();
    private AnnounceReceiver announceReceiver;
    private int myId = -1;
    private ServerItem currentServer;

    public void start(Consumer<ServerItem> onNewAnnouncement, int port) throws IOException {
        this.onNewAnnouncement = onNewAnnouncement;
        messagesHandler = new ClientMessagesHandler(this);
        networkManager = new NetworkActionsManager(messagesHandler, port);
        networkManager.start();
        announceReceiver = new AnnounceReceiver(messagesHandler);
        announceReceiver.start();
    }

    public void newAnnouncement(ServerItem item) {
        // Todo save new server id or call it in callback
        serversRepository.addServerItem(item);
        onNewAnnouncement.accept(item);
        // Tests only
        System.out.println("Available Servers :");
        serversRepository.getServersCopy().forEach(System.out::println);
        System.out.println("-------------------");
    }

    public void joinGame(int serverItemId, String userName) {
        serversRepository.setServerToConnectId(serverItemId);
        networkManager.putSendTask(new SendTask(
                new JoinMessage(userName, MessagesCounter.next(), 1000, 0),
                serversRepository.getServersCopy().get(serverItemId).getAddress()));
    }

    public void rotate(Snake.Direction direction) {
        networkManager.putSendTask(new SendTask(
                new SteerMessage(direction, MessagesCounter.next(), myId, 0),
                currentServer.getAddress()));
    }

    public int getMyId() {
        return myId;
    }

    public void setMyId(int myId) {
        this.myId = myId;
    }

    public ServerItem getCurrentServer() {
        return currentServer;
    }

    public void acceptServer() {
        this.currentServer = serversRepository.acceptServer();
    }

    public void setCurrentServer(ServerItem currentServer) {
        this.currentServer = currentServer;
    }
}
