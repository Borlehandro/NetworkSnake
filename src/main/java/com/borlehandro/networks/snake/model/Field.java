package com.borlehandro.networks.snake.model;

public class Field {

    private int size;

    // Field nodes table
    private FieldNode[][] fieldMatrix;

    public Field(int size) {
        this.size = size;
        fieldMatrix = new FieldNode[size][size];
        // TODO Refactor
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                fieldMatrix[i][j] = new FieldNode(i, j);
            }
        }
    }

    public int toLoop(int coordinate) {
        if (coordinate < 0)
            return size + coordinate;
        else if (coordinate > size - 1)
            return coordinate - size;
        else
            return coordinate;
    }

    public FieldNode[][] getFieldMatrix() {
        return fieldMatrix;
    }

    public int getSize() {
        return size;
    }
}
