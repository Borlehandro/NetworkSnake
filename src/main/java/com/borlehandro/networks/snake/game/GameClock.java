package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.protocol.GameConfig;

public class GameClock extends Thread {
    private final int TICK_TIMEOUT;
    private final GameSession session;

    public GameClock(GameSession session, GameConfig config) {
        TICK_TIMEOUT = config.getStateDelayMillis();
        this.session = session;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                sleep(TICK_TIMEOUT);
            } catch (InterruptedException e) {
                return;
            }
            session.nextStep();
        }
    }
}
