package com.borlehandro.networks.snake.spawn;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FoodSpawner {
    private final Field field;
    private final int RANDOM_FOOD_NUMBER = 12;

    public FoodSpawner(Field field) {
        this.field = field;
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
        // Select empty rows
        matrixCopy.stream()
                .filter(fieldNode -> fieldNode.getState().equals(FieldNode.State.EMPTY))
                .limit(RANDOM_FOOD_NUMBER)
                .forEach(fieldNode -> fieldNode.setState(FieldNode.State.WITH_FOOD));
    }

}
