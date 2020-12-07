package com.borlehandro.networks.snake.game.spawn;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.GameConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FoodSpawner {
    private final Field field;
    private final int staticFood;
    private final int foodPerPlayer;
    private final PlayersServersRepository repository = PlayersServersRepository.getInstance();

    public FoodSpawner(Field field, GameConfig config) {
        this.field = field;
        staticFood = config.getFoodStatic();
        foodPerPlayer = config.getFoodPerPlayer();
    }

    public boolean spawnFoodByCoordinates(int x, int y) {
        var fieldNode = field.getFieldMatrix()[x][y];
        if (fieldNode.getState().equals(FieldNode.State.EMPTY)) {
            fieldNode.setState(FieldNode.State.WITH_FOOD);
            return true;
        } else {
            return false;
        }
    }

    public void spawnRandom() {
        // Shuffle for random
        List<FieldNode> matrixCopy = new ArrayList<>();
        for (FieldNode[] row : field.getFieldMatrix()) {
            matrixCopy.addAll(Arrays.asList(row));
        }
        Collections.shuffle(matrixCopy);
        // Spawn
        try {
            matrixCopy.stream()
                    .filter(fieldNode -> fieldNode.getState().equals(FieldNode.State.EMPTY))
                    .limit((staticFood + foodPerPlayer * repository.getPlayersNumber()) - field.getCurrentFood())
                    .forEach(fieldNode -> {
                        fieldNode.setState(FieldNode.State.WITH_FOOD);
                        field.addFood(1);
                    });
        } catch (IllegalArgumentException ignored) {
        }
    }

}
