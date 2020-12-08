package com.borlehandro.networks.snake.game.session;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

public class GameClock extends Thread {
    private final int TICK_TIMEOUT;
    private final ServerSession session;

    public GameClock(ServerSession session, SnakesProto.GameConfig config) {
        TICK_TIMEOUT = config.getStateDelayMs();
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
