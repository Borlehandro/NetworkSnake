package com.borlehandro.networks.snake.model;

import java.util.List;

public class Field {

    private final int height;
    private final int wight;

    private int currentFood;

    // Field nodes table
    private FieldNode[][] fieldMatrix;

    public Field(int height, int wight) {
        this.height = height;
        this.wight = wight;
        fieldMatrix = new FieldNode[wight][height];
        // TODO Refactor
        for (int i = 0; i < wight; ++i) {
            for (int j = 0; j < height; ++j) {
                fieldMatrix[i][j] = new FieldNode(i, j);
            }
        }
    }

    public int getCurrentFood() {
        return currentFood;
    }

    public void addFood(int newFood) {
        this.currentFood += newFood;
    }

    public void decrementFood() {
        this.currentFood--;
    }

    public enum Axis {
        X, Y
    }

    public int toLoop(int coordinate, Axis axis) {

        int size = switch (axis) {
            case X -> wight;
            case Y -> height;
        };

        if (coordinate < 0)
            return  size + coordinate;
        else if (coordinate > size - 1)
            return   coordinate - size;
        else
            return coordinate;
    }

    public void clear() {
        fieldMatrix = new FieldNode[wight][height];
        // Todo refactor
        for (int i = 0; i < wight; ++i) {
            for (int j = 0; j < height; ++j) {
                fieldMatrix[i][j] = new FieldNode(i, j);
            }
        }
    }

    public void setFood(List<Coordinates> foodCoordinates) {
        foodCoordinates.forEach(coordinate ->
                fieldMatrix[coordinate.getX()][coordinate.getY()].setState(FieldNode.State.WITH_FOOD));
        currentFood = foodCoordinates.size();
    }

    public void setSnakes(List<Snake> snakes) {
        // Todo test
        snakes.forEach(snake -> {
            var body = snake.getBody();
            body.forEach(snakeNode -> {
                if (snakeNode.equals(body.getFirst())) {
                    fieldMatrix[snakeNode.getX()][snakeNode.getY()].setState(FieldNode.State.WITH_SNAKE_HEAD);
                    fieldMatrix[snakeNode.getX()][snakeNode.getY()].addSnake(snake, true);
                } else {
                    fieldMatrix[snakeNode.getX()][snakeNode.getY()].setState(FieldNode.State.WITH_SNAKE_BODY);
                    fieldMatrix[snakeNode.getX()][snakeNode.getY()].addSnake(snake, false);
                }
            });
        });
    }

    public FieldNode[][] getFieldMatrix() {
        return fieldMatrix;
    }
}
