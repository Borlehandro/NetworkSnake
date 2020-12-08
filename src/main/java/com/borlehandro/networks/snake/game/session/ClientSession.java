package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.game.api.AbstractClientSession;
import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.message_handlers.ClientMessagesHandler;
import com.borlehandro.networks.snake.messages.factory.MessageFactory;
import com.borlehandro.networks.snake.model.*;
import com.borlehandro.networks.snake.network.*;
import com.borlehandro.networks.snake.protobuf.SnakesProto;
import com.borlehandro.networks.snake.ui.GameUiController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.borlehandro.networks.snake.protobuf.SnakesProto.NodeRole.*;

public class ClientSession implements AbstractClientSession {
    private BiConsumer<Integer, ServerItem> onNewAnnouncement;
    private GameUiController uiController;
    private final PlayersServersRepository playersServersRepository = PlayersServersRepository.getInstance();
    private Pinger pinger;
    private SnakesProto.GameConfig currentServerConfig;
    private Field field;
    private int myId = -1;
    private int currentStateOrder = -1;
    private boolean isDeputy = false;
    private ServerItem currentServer;
    private List<Snake> snakes;

    private ClientMessagesHandler messagesHandler;
    private NetworkActionsManager networkManager;
    private RepeatController repeatController;
    private OfflineMonitor offlineMonitor;
    private AnnounceReceiver announceReceiver;

    private MessageFactory messageFactory = MessageFactory.getInstance();

    public void start(BiConsumer<Integer, ServerItem> onNewAnnouncement, int port) throws IOException {
        this.onNewAnnouncement = onNewAnnouncement;
        messagesHandler = new ClientMessagesHandler(this);
        messagesHandler.start();
        networkManager = new NetworkActionsManager(messagesHandler, port);
        networkManager.start();
        announceReceiver = new AnnounceReceiver(messagesHandler);
        announceReceiver.start();
    }

    @Override
    public void newAnnouncement(ServerItem item) {
        if (currentServer == null) {
            // Todo save new server id or call it in callback
            int id = playersServersRepository.addServerItem(item);
            if (id >= 0)
                onNewAnnouncement.accept(id, item);
            // Tests only
            System.out.println("Available Servers :");
            playersServersRepository.getServersCopy().forEach(System.out::println);
            System.out.println("-------------------");
        }
    }

    @Override
    public void joinGame(int serverItemId, String userName) {
        playersServersRepository.setServerToConnectId(serverItemId);
        networkManager.putSendTask(new SendTask(
                // Todo fix to normal id
                messageFactory.createJoinMessage(MessagesCounter.next(), 1000, 0, userName),
                playersServersRepository.getServersCopy().get(serverItemId).getAddress()));
    }

    @Override
    public void rotate(Snake.Direction direction) {
        networkManager.putSendTask(new SendTask(
                messageFactory.createSteerMessage(MessagesCounter.next(), myId, 0, direction),
                currentServer.getAddress()));
    }

    @Override
    public void setController(GameUiController controller) {
        uiController = controller;
    }

    public void updateState(int stateOrder,
                            List<Snake> snakes,
                            List<SnakesProto.GameState.Coord> foodCoordinates,
                            Collection<Player> players,
                            SnakesProto.GameConfig config) {
        if (stateOrder > currentStateOrder && myId > 0) {
            players.forEach(playersServersRepository::putPlayer);
            // Just update field
            if (this.field == null)
                field = new Field(config.getHeight(), config.getWidth());
            field.clear();
            field.setFood(foodCoordinates);
            field.setSnakes(snakes);
            this.snakes = snakes;
            currentStateOrder = stateOrder;
            if(uiController!=null) {
                // Todo test UI
                uiController.onStateUpdate(field, playersServersRepository.getPlayersMap());
            }
            // Test only
            // Field showing
            var matrix = field.getFieldMatrix();
            System.out.println("---------------------");
            // TODO Replace length with height and wight
            for (int i = matrix.length - 1; i >= 0; --i) {
                for (int j = 0; j < matrix.length; ++j) {
                    // J,I!
                    switch (matrix[j][i].getState()) {
                        case EMPTY -> System.out.print("O");
                        case WITH_SNAKE_HEAD -> System.out.print("*");
                        case WITH_FOOD -> System.out.print("F");
                        case WITH_SNAKE_BODY -> System.out.print("+");
                    }
                    System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println("---------------------");
        } else {
            // Test only
            System.err.println("UPDATE IGNORED number " + stateOrder + "<=" + currentStateOrder);
        }
    }

    public void onRolesChanged(NodeRole senderRole, NodeRole myRole, int senderId) {
        // Todo handle normally
        if (myRole.equals(NodeRole.VIEWER)) {
            System.err.println("I'm viewer");
        }
        if (myRole.equals(NodeRole.DEPUTY)) {
            System.err.println("I'm deputy");
            isDeputy = true;
            // Todo set deputy flag and if server offline
            //  automatically become master
        }
        if (myRole.equals(NodeRole.MASTER)) {
            System.err.println("I'm new master");
            synchronized (playersServersRepository) {
                playersServersRepository.getPlayers().forEach(player ->
                        // Todo use myId or 0?
                        networkManager.putSendTask(new SendTask(
                                messageFactory.createRoleChangeMessage(MessagesCounter.next(), myId, player.getId(), MASTER, NORMAL),
                                playersServersRepository.findPlayerAddressById(player.getId()).get()
                        ))
                );
            }
        }
        if (senderRole.equals(NodeRole.MASTER) && senderId != 0) {
            System.err.println("New master O_O");
            // Todo test change currentServerItem and clear receive time
            currentServer.changeSocketAddress(playersServersRepository.findPlayerAddressById(senderId).get());
            playersServersRepository.getLastReceivedMessageTimeMillis().replace(
                    playersServersRepository.getCurrentServerId(),
                    System.currentTimeMillis() + currentServerConfig.getStateDelayMs()
            );
        }
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

    public void acceptServer(int newMyId) {
        // TODO Test
        if (currentServer == null) {
            myId = newMyId;
            System.out.println("Player joined " + myId);
            // Get first config.
            // Launch Pinger and RepeatController
            synchronized (playersServersRepository) {
                this.currentServer = playersServersRepository.acceptServer();
                currentServerConfig = currentServer.getConfig();
                playersServersRepository.getLastSentMessageTimeMillis().put(0, System.currentTimeMillis());
                pinger = new Pinger(currentServerConfig, networkManager, myId, playersServersRepository.getLastSentMessageTimeMillis(),
                        playersServersRepository::findServerSocketAddressById
                );
                pinger.start();
                repeatController = new RepeatController(
                        networkManager,
                        networkManager.getWaitResponseMessages(),
                        currentServerConfig, playersServersRepository.getLastSentMessageTimeMillis());
                repeatController.start();
                playersServersRepository.getLastReceivedMessageTimeMillis().put(
                        0, System.currentTimeMillis() + currentServerConfig.getStateDelayMs()
                );
                offlineMonitor = new OfflineMonitor(
                        this,
                        currentServerConfig,
                        playersServersRepository.getLastReceivedMessageTimeMillis()
                );
                offlineMonitor.start();
            }
            networkManager.setMyId(newMyId);
        }
    }

    @Override
    public void onNodeOffline(int serverId) {
        if (isDeputy && serverId == playersServersRepository.getCurrentServerId()) {
            System.err.println("NODE OFFLINE: " + serverId + " time " + System.currentTimeMillis() + " last message " + playersServersRepository.getLastReceivedMessageTimeMillis().get(serverId));
            System.err.println("I'm master, because server offline");
            // Todo do it when relaunch
            playersServersRepository.getPlayers().forEach(player -> {
                if (player.getId() > 0 && player.getId() != myId) {
                    networkManager.putSendTask(new SendTask(
                            messageFactory.createRoleChangeMessage(MessagesCounter.next(), myId, player.getId(), MASTER, NORMAL),
                            new InetSocketAddress(player.getIpAddress(), player.getPort())
                    ));
                }
            });
            // Todo test
            isDeputy = false;
            System.err.println("223");
            messagesHandler.interrupt();
            pinger.interrupt();
            repeatController.interrupt();
            // networkManager.interrupt();
            announceReceiver.interrupt();
            System.err.println("228");

            Map<Integer, Snake> snakeMap = new HashMap<>();
            snakes.forEach(
                    snake -> {
                        if (snake.getPlayerId() == myId) {
                            snake.changePlayerId(0);
                        } else if (snake.getPlayerId() == 0) {
                            snake.changePlayerId(-1);
                            snake.setZombie();
                        }
                        snakeMap.put(snake.getPlayerId(), snake);
                    }
            );

            playersServersRepository
                    .getPlayers()
                    .removeIf(player -> player.getId() == 0);

            // Todo test
            playersServersRepository.clearServers();
            playersServersRepository.getLastReceivedMessageTimeMillis().clear();
            playersServersRepository.getLastSentMessageTimeMillis().clear();

            // Add players to last send and receive

            var latsSend = playersServersRepository.getLastSentMessageTimeMillis();
            var lastReceive = playersServersRepository.getLastReceivedMessageTimeMillis();

            playersServersRepository.changePlayerId(myId, 0);

            playersServersRepository
                    .getPlayers()
                    .forEach(player -> {
                        if (player.getId() > 0) {
                            latsSend.put(player.getId(), 0L);
                            lastReceive.put(player.getId(),
                                    System.currentTimeMillis() + currentServerConfig.getNodeTimeoutMs()
                            );
                        }
                    });
            System.err.println("270");
            try {
                ServerSession session = new ServerSession(currentServerConfig, snakeMap, field);
                session.setController(uiController);
                uiController.changeSession(session);

                session.startWithContext(networkManager, currentStateOrder);
                System.err.println("276");
            } catch (SocketException e) {
                e.printStackTrace();
            }
            offlineMonitor.interrupt();
        }
    }

    @Override
    public void exit() {
        networkManager.putSendTask(new SendTask(
                messageFactory.createRoleChangeMessage(MessagesCounter.next(), myId, 0, VIEWER, MASTER),
                currentServer.getAddress()
        ));

        // Todo interrupt all threads
        System.err.println("EXIT !!!");
        networkManager.interrupt();
        pinger.interrupt();
        offlineMonitor.interrupt();
        repeatController.interrupt();
        messagesHandler.interrupt();
        announceReceiver.interrupt();

    }
}
