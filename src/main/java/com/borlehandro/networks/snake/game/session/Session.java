package com.borlehandro.networks.snake.game.session;

public interface Session {
    void onNodeOffline(int nodeId);
    void exit();
}
