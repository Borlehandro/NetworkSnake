package com.borlehandro.networks.snake.game.api;

import com.borlehandro.networks.snake.model.ServerItem;

public interface AbstractClientSession extends Session {
    void newAnnouncement(ServerItem item);
    void joinGame(int serverItemId, String userName);
    void setAbstractController(AbstractController abstractController);
}
