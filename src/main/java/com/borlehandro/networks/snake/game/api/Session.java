package com.borlehandro.networks.snake.game.api;

import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.ui.GameUiController;

public interface Session {
    void onNodeOffline(int nodeId);
    default void rotate(int id, Snake.Direction direction){
        throw new IllegalCallerException("This method should never be called");
    }
    default void rotate(Snake.Direction direction) {
        rotate(0, direction);
    }
    void setController(GameUiController controller);
    void exit();
}
