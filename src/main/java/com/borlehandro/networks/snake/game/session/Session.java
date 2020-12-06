package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.model.Snake;

public interface Session {
    void onNodeOffline(int nodeId);
    default void rotate(int id, Snake.Direction direction){
        throw new IllegalCallerException("This method should never be called");
    }
    default void rotate(Snake.Direction direction) {
        rotate(0, direction);
    }
    void exit();
}
