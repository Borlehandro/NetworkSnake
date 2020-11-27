package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.PlayersServersRepository;
import com.borlehandro.networks.snake.game.CollisionHandler;
import com.borlehandro.networks.snake.game.MoveController;
import com.borlehandro.networks.snake.game.ScoreManager;
import com.borlehandro.networks.snake.game.SnakesCollisionController;
import com.borlehandro.networks.snake.game.spawn.FoodSpawner;
import com.borlehandro.networks.snake.game.spawn.SnakeSpawner;
import com.borlehandro.networks.snake.message_handlers.ServerMessagesHandler;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.network.MessagesCounter;
import com.borlehandro.networks.snake.network.MulticastClock;
import com.borlehandro.networks.snake.network.NetworkActionsManager;
import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.NodeRole;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.SendTask;
import com.borlehandro.networks.snake.protocol.messages.action.AckMessage;
import com.borlehandro.networks.snake.protocol.messages.action.ErrorMessage;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class ServerSession {
    private NetworkActionsManager networkManager;
    private final PlayersServersRepository playersRepository = PlayersServersRepository.getInstance();
    private final ScoreManager scoreManager = new ScoreManager();
    private final Map<Integer, Snake> snakeMap = new HashMap<>();
    private final Map<Integer, Snake.Direction> rotationsPool = new HashMap<>();
    private final Field field;
    private final GameConfig config;
    private final MoveController moveController;
    private final SnakesCollisionController collisionController;
    private final FoodSpawner foodSpawner;
    private final SnakeSpawner snakeSpawner;
    private ServerMessagesHandler messagesHandler;
    private GameClock gameClock;
    private MulticastClock multicastClock;

    public ServerSession(GameConfig config) {
        this.config = config;
        field = new Field(config.getFieldHeight(), config.getFieldWidth());
        moveController = new MoveController(field);
        collisionController = new SnakesCollisionController(field, snakeMap.values(), config);
        foodSpawner = new FoodSpawner(field, config);
        snakeSpawner = new SnakeSpawner(field, snakeMap);
    }

    public void start(int port) throws SocketException {
        messagesHandler = new ServerMessagesHandler(this);
        this.networkManager = new NetworkActionsManager(messagesHandler, port);
        gameClock = new GameClock(this, config);
        // Todo Add master player and snake.
        Player admin = Player.builder()
                .withId(0)
                .withName("Admin")
                .withIpAddress("0.0.0.0")
                .withPort(8080)
                .withRole(NodeRole.MASTER)
                .build().get();
        addPlayer(admin);
        // Todo Start clocks.
        gameClock.start();
        networkManager.start();
        // Todo Start multicast messages sending.
        multicastClock = new MulticastClock(networkManager, config);
        multicastClock.start();
    }

    // Called in PlayerRepository thread
    public void addPlayer(Player player) {
        synchronized (snakeMap) {
            player.setId(playersRepository.addPlayer(player));
            if (!snakeSpawner.spawnRandom(player.getId())) {
                // Todo Is my id always 0?
                // Player id is always -1
                player.setRole(NodeRole.VIEWER);
                networkManager.putSendTask(new SendTask(new ErrorMessage("Can not add player. You are viewer",
                        MessagesCounter.next(),
                        0,
                        player.getId()),
                        playersRepository.findPlayerAddressById(player.getId())));
                // Todo add player as VIEWER
            } else {
                player.setRole(NodeRole.NORMAL);
                networkManager.putSendTask(new SendTask(
                        new AckMessage(MessagesCounter.next(), 0, player.getId()),
                        playersRepository.findPlayerAddressById(player.getId())
                ));
            }
        }
    }

    // Called in GameClocks thread
    void nextStep() {
        synchronized (snakeMap) {
            synchronized (rotationsPool) {
                // Rotate all snakes in rotationPool
                // TODO Use iterator and remove from pool
                rotationsPool.forEach((id, direction) -> moveController.rotate(snakeMap.get(id), direction));
                rotationsPool.clear();
                // Move all snakes
                snakeMap.values().forEach(moveController::moveForward);
                // Handle snakes collisions
                collisionController.handleCollision((CollisionHandler::handle));
                // Spawn food
                foodSpawner.spawnRandom();
            }
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
    }

    public void rotate(int playerId, Snake.Direction direction) {
        synchronized (rotationsPool) {
            rotationsPool.put(playerId, direction);
        }
    }

    public ServerMessagesHandler getMessagesHandler() {
        return messagesHandler;
    }
}
