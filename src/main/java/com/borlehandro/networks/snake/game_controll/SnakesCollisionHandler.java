package com.borlehandro.networks.snake.game_controll;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Snake;

import java.util.*;
import java.util.function.Consumer;

public class SnakesCollisionHandler {
    private final Field field;
    private final Collection<Snake> snakes;

    public SnakesCollisionHandler(Field field, Collection<Snake> snakes) {
        this.field = field;
        this.snakes = snakes;
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
        crashedSnakes.forEach((snake -> {
            snake.getBody().forEach(node -> {
                var fieldNode = field.getFieldMatrix()[node.getX()][node.getY()];
                fieldNode.removeSnake(snake);
                fieldNode.setState(FieldNode.State.WITH_FOOD); // Todo Do it randomly
            });
            snakes.remove(snake);
            onSnakeCrash.accept(snake);
        }));
    }

}
