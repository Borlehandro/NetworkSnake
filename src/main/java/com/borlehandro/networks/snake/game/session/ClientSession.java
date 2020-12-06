package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.ConsoleController;
import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.message_handlers.ClientMessagesHandler;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.network.*;
import com.borlehandro.networks.snake.protocol.*;
import com.borlehandro.networks.snake.protocol.messages.action.JoinMessage;
import com.borlehandro.networks.snake.protocol.messages.action.RoleChangeMessage;
import com.borlehandro.networks.snake.protocol.messages.action.SteerMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ClientSession implements Session {
    private Consumer<ServerItem> onNewAnnouncement;
    private ClientMessagesHandler messagesHandler;
    private NetworkActionsManager networkManager;
    private ConsoleController consoleController;
    private final PlayersServersRepository playersServersRepository = PlayersServersRepository.getInstance();
    private AnnounceReceiver announceReceiver;
    private Pinger pinger;
    private GameConfig currentServerConfig;
    private Field field;
    private int myId = -1;
    private int currentStateOrder = -1;
    private boolean isDeputy = false;
    private ServerItem currentServer;
    private RepeatController repeatController;
    private OfflineMonitor offlineMonitor;
    private List<Snake> snakes;

    public void start(Consumer<ServerItem> onNewAnnouncement, int port) throws IOException {
        this.onNewAnnouncement = onNewAnnouncement;
        messagesHandler = new ClientMessagesHandler(this);
        messagesHandler.start();
        networkManager = new NetworkActionsManager(messagesHandler, port);
        networkManager.start();
        announceReceiver = new AnnounceReceiver(messagesHandler);
        announceReceiver.start();
    }

    public void newAnnouncement(ServerItem item) {
        if (currentServer == null) {
            // Todo save new server id or call it in callback
            playersServersRepository.addServerItem(item);
            onNewAnnouncement.accept(item);
            // Tests only
            System.out.println("Available Servers :");
            playersServersRepository.getServersCopy().forEach(System.out::println);
            System.out.println("-------------------");
        }
    }

    public void joinGame(int serverItemId, String userName) {
        playersServersRepository.setServerToConnectId(serverItemId);
        networkManager.putSendTask(new SendTask(
                new JoinMessage(userName, MessagesCounter.next(), 1000, 0),
                playersServersRepository.getServersCopy().get(serverItemId).getAddress()));
    }

    @Override
    public void rotate(Snake.Direction direction) {
        networkManager.putSendTask(new SendTask(
                new SteerMessage(direction, MessagesCounter.next(), myId, 0),
                currentServer.getAddress()));
    }

    public void updateState(int stateOrder,
                            List<Snake> snakes,
                            List<Coordinates> foodCoordinates,
                            Collection<Player> players,
                            GameConfig config) {
        if (stateOrder > currentStateOrder) {
            players.forEach(playersServersRepository::putPlayer);
            // Just update field
            if (this.field == null)
                field = new Field(config.getFieldHeight(), config.getFieldWidth());
            field.clear();
            field.setFood(foodCoordinates);
            field.setSnakes(snakes);
            this.snakes = snakes;
            currentStateOrder = stateOrder;
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
                                new RoleChangeMessage(NodeRole.MASTER, NodeRole.NORMAL, MessagesCounter.next(), myId, player.getId()),
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
                    System.currentTimeMillis() + currentServerConfig.getStateDelayMillis()
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
                        currentServerConfig);
                repeatController.start();
                playersServersRepository.getLastReceivedMessageTimeMillis().put(
                        0, System.currentTimeMillis() + currentServerConfig.getStateDelayMillis()
                );
                offlineMonitor = new OfflineMonitor(
                        this,
                        currentServerConfig,
                        playersServersRepository.getLastReceivedMessageTimeMillis()
                );
                offlineMonitor.start();
            }
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
                            new RoleChangeMessage(NodeRole.MASTER, NodeRole.NORMAL, MessagesCounter.next(), myId, player.getId()),
                            new InetSocketAddress(player.getIpAddress(), player.getPort())
                    ));
                }
            });
            // Todo test
            isDeputy = false;

            messagesHandler.interrupt();
            pinger.interrupt();
            repeatController.interrupt();
            // networkManager.interrupt();
            announceReceiver.interrupt();

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
                                    System.currentTimeMillis() + currentServerConfig.getNodeTimeoutMillis()
                            );
                        }
                    });

            try {
                ServerSession session = new ServerSession(consoleController, currentServerConfig, snakeMap, field);
                consoleController.changeSession(session);
                session.startWithContext(networkManager, currentStateOrder);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            offlineMonitor.interrupt();
        }
    }

    @Override
    public void exit() {
        networkManager.putSendTask(new SendTask(
                new RoleChangeMessage(NodeRole.VIEWER, NodeRole.MASTER, MessagesCounter.next(), myId, 0),
                currentServer.getAddress()
        ));
        // Todo interrupt all threads
    }

    public void setConsoleController(ConsoleController consoleController) {
        this.consoleController = consoleController;
    }
}
