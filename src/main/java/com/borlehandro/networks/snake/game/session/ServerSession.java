package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.game.CollisionHandler;
import com.borlehandro.networks.snake.game.MoveController;
import com.borlehandro.networks.snake.game.SnakesCollisionController;
import com.borlehandro.networks.snake.game.api.Session;
import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.game.spawn.FoodSpawner;
import com.borlehandro.networks.snake.game.spawn.SnakeSpawner;
import com.borlehandro.networks.snake.message_handlers.ServerMessagesHandler;
import com.borlehandro.networks.snake.messages.factory.MessageFactory;
import com.borlehandro.networks.snake.model.*;
import com.borlehandro.networks.snake.network.*;
import com.borlehandro.networks.snake.protobuf.SnakesProto;
import com.borlehandro.networks.snake.ui.GameUiController;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.borlehandro.networks.snake.protobuf.SnakesProto.NodeRole.*;

public class ServerSession implements Session {
    private GameUiController uiController;
    private final PlayersServersRepository playersRepository = PlayersServersRepository.getInstance();
    private final Map<Integer, Snake> snakeMap;
    private final Map<Integer, Snake.Direction> rotationsPool = new HashMap<>();
    private final Field field;
    private final SnakesProto.GameConfig config;
    private final MoveController moveController;
    private final SnakesCollisionController collisionController;
    private final FoodSpawner foodSpawner;
    private final SnakeSpawner snakeSpawner;
    private CollisionHandler collisionHandler;
    private final MessageFactory messageFactory = MessageFactory.getInstance();
    private NetworkActionsManager networkManager;
    private ServerMessagesHandler messagesHandler;
    private OfflineMonitor offlineMonitor;
    private GameClock gameClock;
    private MulticastClock multicastClock;
    private RepeatController repeatController;
    private Pinger pinger;

    private final AtomicInteger stateOrder = new AtomicInteger(0);
    private int deputyId = -1;

    public ServerSession(SnakesProto.GameConfig config) {
        this.config = config;
        field = new Field(config.getHeight(), config.getWidth());
        snakeMap = new HashMap<>();
        moveController = new MoveController(field);
        collisionController = new SnakesCollisionController(field, snakeMap.values(), config);
        foodSpawner = new FoodSpawner(field, config);
        snakeSpawner = new SnakeSpawner(field, snakeMap);
        messageFactory.initGameStateMessageFactory(snakeMap,
                config,
                playersRepository.getPlayers(),
                field,
                stateOrder);
        messageFactory.initAnnouncementMessageFactory(playersRepository.getPlayers(), config);
    }

    public ServerSession(SnakesProto.GameConfig config, Map<Integer, Snake> snakeMap, Field field) {
        this.config = config;
        this.field = field;
        this.snakeMap = snakeMap;
        moveController = new MoveController(this.field);
        collisionController = new SnakesCollisionController(this.field, this.snakeMap.values(), this.config);
        foodSpawner = new FoodSpawner(this.field, this.config);
        snakeSpawner = new SnakeSpawner(this.field, this.snakeMap);
        messageFactory.initGameStateMessageFactory(
                this.snakeMap,
                this.config,
                playersRepository.getPlayers(),
                this.field,
                stateOrder
        );
    }

    public void startWithContext(NetworkActionsManager networkManager, int stateOrderValue) throws SocketException {
        messagesHandler = new ServerMessagesHandler(this);
        System.err.println("90");
        synchronized (networkManager) {
            networkManager.changeMessageHandler(messagesHandler);
        }
        System.err.println("95");
        this.networkManager = networkManager;
        gameClock = new GameClock(this, config);
        collisionHandler = new CollisionHandler(snakeMap, this);
        stateOrder.set(stateOrderValue);
        playersRepository.setPlayersNumber(playersRepository.getPlayers().size());
        System.err.println("101");
        // Todo test
        var opt = playersRepository.getPlayers().stream().filter(player -> player.getId() > 0).findAny();
        opt.ifPresent(player ->
                networkManager.putSendTask(
                        new SendTask(
                                SnakesProto.GameMessage.newBuilder()
                                        .setSenderId(0)
                                        .setReceiverId(player.getId())
                                        .setMsgSeq(MessagesCounter.next())
                                        .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                                                .setSenderRole(MASTER)
                                                .setReceiverRole(DEPUTY).build())
                                        .build(),
                                playersRepository.findPlayerAddressById(player.getId()).get()))
        );
        init();
    }

    public void init() {
        System.err.println("111");
        gameClock.start();
        messagesHandler.start();
        networkManager.setMyId(0);
        if (!networkManager.isAlive())
            networkManager.start();
        System.err.println("117");
        multicastClock = new MulticastClock(networkManager, config);
        multicastClock.start();
        offlineMonitor = new OfflineMonitor(this, config, playersRepository.getLastReceivedMessageTimeMillis());
        offlineMonitor.start();
        repeatController = new RepeatController(networkManager, networkManager.getWaitResponseMessages(), config, playersRepository.getLastSentMessageTimeMillis());
        repeatController.start();
        pinger = new Pinger(config, networkManager, 0, playersRepository.getLastSentMessageTimeMillis(),
                id -> playersRepository.findPlayerAddressById(id).get()
        );
        pinger.start();
        System.err.println("Start successful");
    }

    public void start(int port) throws SocketException {
        messagesHandler = new ServerMessagesHandler(this);
        this.networkManager = new NetworkActionsManager(messagesHandler, port);
        gameClock = new GameClock(this, config);
        collisionHandler = new CollisionHandler(snakeMap, this);
        // Todo Use normal ip, because changed master
        //  can send message to us?
        Player admin = Player.builder()
                .withId(0)
                .withName("Admin")
                .withIpAddress("0.0.0.0")
                .withPort(port)
                .withRole(NodeRole.MASTER)
                .build().get();
        addPlayer(admin, -1);
        init();
    }

    // Called in PlayerRepository thread
    // Todo test synchronized
    public void addPlayer(Player player, long messageNumber) {
        System.err.println("Wait snake monitor " + System.currentTimeMillis());
        synchronized (snakeMap) {
            System.err.println("Wait repo monitor " + System.currentTimeMillis());
            // Todo test without player repo synchronization
            synchronized (playersRepository) {
                System.err.println("Adding player..." + System.currentTimeMillis());
                player.setId(playersRepository.addPlayer(player));
                if (player.getId() != 0)
                    playersRepository.getLastSentMessageTimeMillis().put(player.getId(), System.currentTimeMillis());
                // Last time is realTime + stateDelay
                System.err.println("156 " + System.currentTimeMillis());
                playersRepository.updateLastReceivedMessageTimeMillis(
                        player.getId(),
                        System.currentTimeMillis() + config.getStateDelayMs(),
                        true
                );
                System.err.println("162 " + System.currentTimeMillis());
                if (!snakeSpawner.spawnRandom(player.getId())) {
                    System.err.println("Can not add");
                    // Todo Is my id always 0?
                    // Player id is always -1
                    player.setRole(NodeRole.VIEWER);
                    networkManager.putSendTask(new SendTask(
                            messageFactory.createErrorMessage(
                                    MessagesCounter.next(),
                                    0,
                                    player.getId(),
                                    "Can not add player. You are viewer"),
                            playersRepository.findPlayerAddressById(player.getId()).get()
                    ));
                } else {
                    System.err.println("Add successful " + player.getId());
                    player.setRole(NodeRole.NORMAL);
                    // Dont send message to myself
                    if (player.getId() != 0) {
                        networkManager.putSendTask(new SendTask(
                                messageFactory.createAckMessage(messageNumber, 0, player.getId()),
                                playersRepository.findPlayerAddressById(player.getId()).get()
                        ));
                        // Todo test
                        if (deputyId == -1) {
                            deputyId = player.getId();
                            System.err.println("Set deputy: " + deputyId);
                            networkManager.putSendTask(new SendTask(
                                    messageFactory.createRoleChangeMessage(MessagesCounter.next(), 0, player.getId(), MASTER, DEPUTY),
                                    playersRepository.findPlayerAddressById(player.getId()).get()
                            ));
                        }
                    }
                }
            }
        }
    }

    // Called in GameClocks thread
    // Todo test synchronized
    void nextStep() {
        System.err.println("Start next step: " + System.currentTimeMillis());
        synchronized (snakeMap) {
            synchronized (rotationsPool) {
                System.err.println("Next step all monitors: " + System.currentTimeMillis());
                playersRepository.getPlayersMap()
                        .forEach((integer, player) -> System.out.println(player.toString()));
                // Rotate all snakes in rotationPool
                // TODO Use iterator and remove from pool
                rotationsPool.forEach((id, direction) -> {
                    if (snakeMap.containsKey(id))
                        moveController.rotate(snakeMap.get(id), direction);
                });
                rotationsPool.clear();

                // Move all snakes
                snakeMap.values().forEach(moveController::moveForward);
                // Handle snakes collisions
                collisionController.controlCollisions(collisionHandler::handle);
                // Spawn food
                foodSpawner.spawnRandom();
                stateOrder.incrementAndGet();
                // Send game state to all players
                // Todo wait for condition and make normal concurrency
                playersRepository.getPlayersCopy().forEach(player -> {
                            if (player.getId() != 0) {
                                networkManager.putSendTask(new SendTask(
                                        messageFactory.createStateMessage(MessagesCounter.next(), 0, player.getId()),
                                        new InetSocketAddress(player.getIpAddress(), player.getPort())
                                ));
                            }
                        }
                );
                // Todo start next step only if all messages was sent
                //  but GameClock doesn't know about it...
                //  I must start next clock step when this step will be completed
            }
        }
        if (uiController != null) {
            // Test UI
            uiController.onStateUpdate(field, playersRepository.getPlayersMap());
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
        System.err.println("Finish next step: " + System.currentTimeMillis());
    }

    @Override
    // Todo test synchronized
    public void rotate(int playerId, Snake.Direction direction) {
        synchronized (rotationsPool) {
            rotationsPool.put(playerId, direction);
            System.err.println("Rotate: " + playerId + " to " + direction);
        }
    }

    // Todo test synchronized
    public void onPlayerCrashed(int playerId) {
        if (playerId != 0) {
            var opt = playersRepository.findPlayerAddressById(playerId);
            opt.ifPresent(socketAddress -> {
                        networkManager.putSendTask(new SendTask(
                                messageFactory.createRoleChangeMessage(MessagesCounter.next(), 0, playerId, MASTER, VIEWER),
                                socketAddress
                        ));
                        if (playerId == deputyId) {
                            networkManager.putSendTask(new SendTask(
                                    messageFactory.createRoleChangeMessage(MessagesCounter.next(), 0, playerId, MASTER, DEPUTY),
                                    socketAddress
                            ));
                            // O_o
                        }
                    }
            );
        } else {
            // Todo handle master crash
        }
    }

    @Override
    // Todo test synchronized
    public void onNodeOffline(int playerId) {
        if (playerId > 0) {
            synchronized (playersRepository) {
                System.err.println("Player disconnected: " + playerId + " time " + System.currentTimeMillis());
                playersRepository.removePlayer(playerId);
                playersRepository.getLastSentMessageTimeMillis().remove(playerId);
                playersRepository.getLastReceivedMessageTimeMillis().remove(playerId);
                // Todo test
                if (playerId == deputyId) {
                    if (!playersRepository.getPlayers().isEmpty()) {
                        var opt = playersRepository.getPlayers()
                                .stream().filter(player -> player.getId() > 0).findAny();
                        if (opt.isPresent()) {
                            deputyId = opt.get().getId();
                            networkManager.putSendTask(new SendTask(
                                    messageFactory.createRoleChangeMessage(MessagesCounter.next(), 0, deputyId, MASTER, DEPUTY),
                                    playersRepository.findPlayerAddressById(deputyId).get()
                            ));
                        } else {
                            deputyId = -1;
                        }
                    } else {
                        deputyId = -1;
                    }
                    System.err.println("Set deputy: " + deputyId);
                }
            }
            synchronized (snakeMap) {
                if (snakeMap.containsKey(playerId))
                    snakeMap.get(playerId).setZombie();
            }
        }
    }

    public ServerMessagesHandler getMessagesHandler() {
        return messagesHandler;
    }

    // Todo test synchronized
    public void setViewer(int playerId) {
        synchronized (playersRepository) {
            networkManager.putSendTask(new SendTask(
                    messageFactory.createRoleChangeMessage(MessagesCounter.next(), 0, playerId, MASTER, VIEWER),
                    playersRepository.findPlayerAddressById(playerId).get()
            ));
            synchronized (snakeMap) {
                if (snakeMap.containsKey(playerId))
                    snakeMap.get(playerId).setZombie();
            }
        }
    }

    // Todo test synchronized
    public void exit() {
        System.err.println("EXIT");
        if (deputyId != -1) {
            networkManager.putSendTask(new SendTask(
                    messageFactory.createRoleChangeMessage(MessagesCounter.next(), 0, deputyId, MASTER, MASTER),
                    playersRepository.findPlayerAddressById(deputyId).get()
            ));
        }
        // Todo interrupt threads
        networkManager.interrupt();
        pinger.interrupt();
        offlineMonitor.interrupt();
        messagesHandler.interrupt();
        multicastClock.interrupt();

        gameClock.interrupt();
        repeatController.interrupt();
    }

    @Override
    public void setController(GameUiController controller) {
        uiController = controller;
    }
}