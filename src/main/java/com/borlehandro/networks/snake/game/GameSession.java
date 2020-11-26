package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.PlayersRepository;
import com.borlehandro.networks.snake.game.spawn.FoodSpawner;
import com.borlehandro.networks.snake.game.spawn.SnakeSpawner;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.network.MessagesCounter;
import com.borlehandro.networks.snake.network.NetworkActionsManager;
import com.borlehandro.networks.snake.protocol.GameConfig;
import com.borlehandro.networks.snake.protocol.NodeRole;
import com.borlehandro.networks.snake.protocol.Player;
import com.borlehandro.networks.snake.protocol.messages.action.ErrorMessage;

import java.util.HashMap;
import java.util.Map;

public class GameSession {
    private final NetworkActionsManager networkManager;
    private final PlayersRepository playersRepository = PlayersRepository.getInstance();
    private final ScoreManager scoreManager = new ScoreManager();
    private final Map<Integer, Snake> snakeMap = new HashMap<>();
    private final Map<Integer, Snake.Direction> rotationsPool = new HashMap<>();
    private final Field field;
    private final GameConfig config;
    private final MoveController moveController;
    private final SnakesCollisionController collisionController;
    private final FoodSpawner foodSpawner;
    private final SnakeSpawner snakeSpawner;
    private GameClock clock;

    public GameSession(NetworkActionsManager networkManager, GameConfig config) {
        this.networkManager = networkManager;
        this.config = config;
        field = new Field(config.getFieldHeight(), config.getFieldWidth());
        moveController = new MoveController(field);
        collisionController = new SnakesCollisionController(field, snakeMap.values());
        foodSpawner = new FoodSpawner(field, config);
        snakeSpawner = new SnakeSpawner(field, snakeMap);
    }

    public void start() {
        clock = new GameClock(this, config);
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
        clock.start();

        // Todo Start multicast messages sending.
    }

    // Called in PlayerRepository thread
    public void addPlayer(Player player) {
        synchronized (snakeMap) {
            if(!snakeSpawner.spawnRandom(player.getId())) {
                // Todo Is my id always 0?
                networkManager.putMessage(new ErrorMessage(
                        "Can not add player",
                        MessagesCounter.next(),
                        0,
                        player.getId()));
            } else {
                playersRepository.addPlayer(player);
            }
        }
    }

    // Called in GameClocks thread
    void nextStep() {
        synchronized (snakeMap) {
            synchronized (rotationsPool) {
                // Rotate all snakes in rotationPool
                rotationsPool.forEach((id, direction) -> moveController.rotate(snakeMap.get(id), direction));
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
        for(int i = 0; i <  matrix.length; ++i) {
            for (int j = 0; j < matrix.length; ++j) {
                switch (matrix[i][j].getState()) {
                    case EMPTY -> System.out.print("O");
                    case WITH_SNAKE_HEAD -> System.out.print("H");
                    case WITH_FOOD -> System.out.print("F");
                    case WITH_SNAKE_BODY -> System.out.print("B");
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
}
