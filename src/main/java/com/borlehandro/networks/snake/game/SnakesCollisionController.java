package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.protocol.GameConfig;

import java.util.*;
import java.util.function.Consumer;

public class SnakesCollisionController {
    private final Field field;
    private final Collection<Snake> snakes;
    private final double deadFoodProbability;

    public SnakesCollisionController(Field field, Collection<Snake> snakes, GameConfig config) {
        this.field = field;
        this.snakes = snakes;
        deadFoodProbability = config.getDeadFoodProb();
    }

    // Todo Check "tail on tail" situation
    // Todo Check looped snake
    public void handleCollision(Consumer<Snake> onSnakeCrash) {
        var matrix = field.getFieldMatrix();
        Set<Snake> crashedSnakes = new HashSet<>();
        for (FieldNode[] row : matrix) {
            for (FieldNode node : row) {
                if (node.getBodiesOnTheNode().size() + node.getHeadsOnTheNode().size() > 1) {
                    // Handle collision
                    crashedSnakes.addAll(node.getHeadsOnTheNode());
                }
            }
        }
        // Clear field
        Random random = new Random(System.currentTimeMillis());

        boolean isFood = random.nextInt(100) <= 100 * deadFoodProbability;
        crashedSnakes.forEach((snake -> {
            snake.getBody().forEach(node -> {
                var fieldNode = field.getFieldMatrix()[node.getX()][node.getY()];
                fieldNode.removeSnake(snake);
                // Todo Test this random
                if(isFood) {
                    fieldNode.setState(FieldNode.State.WITH_FOOD);
                    field.addFood(1);
                } else {
                    fieldNode.setState(FieldNode.State.EMPTY);
                }
            });
            snakes.remove(snake);
            onSnakeCrash.accept(snake);
        }));
    }

}
