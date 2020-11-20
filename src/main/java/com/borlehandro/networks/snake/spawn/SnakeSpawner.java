package com.borlehandro.networks.snake.spawn;

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

    public int spawnRandom() {
        var opt = findSpawnPlace();
        if (opt.isPresent()) {
            var direction = Snake.Direction
                    .values()[new Random().nextInt(Snake.Direction.values().length)];
            return spawnSnakeByCoordinates(opt.get().getFirst(), opt.get().getSecond(), direction);
        } else {
            return -1;
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
            for (int squareI = field.toLoop(x - 2), counterI = 0; counterI < 5; squareI = field.toLoop(squareI + 1), counterI++) {
                for (int squareJ = field.toLoop(y - 2), counterJ = 0; counterJ < 5; squareJ = field.toLoop(squareJ + 1), counterJ++) {
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
     *
     * @return integer "snake id" in snakes map or -1 if snake could not be created
     */
    public int spawnSnakeByCoordinates(int headX, int headY, Snake.Direction direction) {
        Snake s = new Snake();
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
        tailX = field.toLoop(tailX);
        tailY = field.toLoop(tailY);
        updateBody(s, headX, headY, direction, true); // Head node
        updateBody(s, tailX, tailY, direction, false); // Body node
        int id = newSnakeId();
        snakes.put(id, s);
        return id;
    }

    // Todo Generate more useful id
    private int newSnakeId() {
        return ++snakesNumber;
    }

    private void updateBody(Snake s, int x, int y, Snake.Direction direction, boolean isHead) {
        s.addNode(x, y, direction);
        var fieldNode = field.getFieldMatrix()[x][y];
        fieldNode.addSnake(s, isHead);
    }
}
