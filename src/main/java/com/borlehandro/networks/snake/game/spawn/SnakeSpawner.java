package com.borlehandro.networks.snake.game.spawn;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Pair;
import com.borlehandro.networks.snake.model.Snake;

import java.util.*;

public class SnakeSpawner {
    private final Field field;
    private final Map<Integer, Snake> snakes;
    private static int snakesNumber = 0;

    public SnakeSpawner(Field field, Map<Integer, Snake> snakes) {
        this.field = field;
        this.snakes = snakes;
    }

    public boolean spawnRandom(int playerId) {
        var opt = findSpawnPlace();
        if (opt.isPresent()) {
            var direction = Snake.Direction
                    .values()[new Random().nextInt(Snake.Direction.values().length)];
            spawnSnakeByCoordinates(opt.get().getFirst(), opt.get().getSecond(), direction, playerId);
            return true;
        } else {
            return false;
        }
    }

    private Optional<Pair<Integer, Integer>> findSpawnPlace() {
        // Shuffle for random
        List<FieldNode> randomizedMatrix = new ArrayList<>();
        for (FieldNode[] row : field.getFieldMatrix()) {
            randomizedMatrix.addAll(Arrays.asList(row));
        }
        Collections.shuffle(randomizedMatrix);
        // Search empty square
        var matrix = field.getFieldMatrix();
        boolean checkMark = true;
        for (FieldNode randomFieldNode : randomizedMatrix) {
            int x = randomFieldNode.getX();
            int y = randomFieldNode.getY();
            square:
            for (int squareI = field.toLoop(x - 2, Field.Axis.X), counterI = 0; counterI < 5; squareI = field.toLoop(squareI + 1, Field.Axis.X), counterI++) {
                for (int squareJ = field.toLoop(y - 2, Field.Axis.Y), counterJ = 0; counterJ < 5; squareJ = field.toLoop(squareJ + 1, Field.Axis.Y), counterJ++) {
                    var state = matrix[squareI][squareJ].getState();
                    boolean cond = !state.equals(FieldNode.State.WITH_SNAKE_BODY) && !state.equals(FieldNode.State.WITH_SNAKE_HEAD);
                    checkMark = cond;
                    if (!cond)
                        break square;
                }
            }
            if (checkMark) {
                return Optional.of(new Pair<>(x, y));
            }
        }
        return Optional.empty();
    }

    /**
     * Create Snake, spawn it in particular field node and insert into snakes map
     */
    public void spawnSnakeByCoordinates(int headX, int headY, Snake.Direction direction, int playerId) {
        Snake s = new Snake(playerId);
        int tailX, tailY;
        switch (direction) {
            case UP -> {
                // Insert down
                tailX = headX;
                tailY = headY - 1;

            }
            case DOWN -> {
                // Insert up
                tailX = headX;
                tailY = headY + 1;

            }
            case RIGHT -> {
                // Insert left
                tailX = headX - 1;
                tailY = headY;

            }
            case LEFT -> {
                // Insert right
                tailX = headX + 1;
                tailY = headY;
            }
            default -> throw new IllegalArgumentException("Wrong direction value");
        }
        tailX = field.toLoop(tailX, Field.Axis.X);
        tailY = field.toLoop(tailY, Field.Axis.Y);
        updateBody(s, headX, headY, direction, true); // Head node
        updateBody(s, tailX, tailY, direction, false); // Body node
        snakes.put(playerId, s);
    }

    private void updateBody(Snake s, int x, int y, Snake.Direction direction, boolean isHead) {
        s.addNode(x, y, direction);
        var fieldNode = field.getFieldMatrix()[x][y];
        fieldNode.addSnake(s, isHead);
    }
}
