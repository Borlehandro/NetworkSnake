package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.game.session.ServerSession;
import com.borlehandro.networks.snake.model.Snake;

import java.util.Map;

public class CollisionHandler {

    // Todo Use BiMap
    private final Map<Integer, Snake> snakeMap;
    private final ServerSession session;

    public CollisionHandler(Map<Integer, Snake> snakeMap, ServerSession session) {
        this.snakeMap = snakeMap;
        this.session = session;
    }

    public void handle(Snake s) {
        System.err.println("COLLISION on " + s);
        var iterator = snakeMap.entrySet().iterator();
        int playerId = -1;
        while (iterator.hasNext()) {
            var entity = iterator.next();
            if (entity.getValue().equals(s)) {
                iterator.remove();
                playerId = entity.getKey();
                break;
            }
        }
        session.onPlayerCrashed(playerId);
    }
}
