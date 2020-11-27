package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.model.Snake;

import java.util.Map;

public class CollisionHandler {
    private final Map<Integer, Snake> snakeMap;

    public CollisionHandler(Map<Integer, Snake> snakeMap) {
        this.snakeMap = snakeMap;
    }

    public static void handle(Snake s) {
        System.err.println("COLLISION on " + s);
        // Todo handle collision
    }
}
