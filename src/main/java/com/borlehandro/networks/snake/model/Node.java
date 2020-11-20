package com.borlehandro.networks.snake.model;

import java.util.Objects;

public class Node {
    protected int x;
    protected int y;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean hasEqualsCoordinates(Node node) {
        return x == node.x && y == node.y;
    }

}