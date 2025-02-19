package com.borlehandro.networks.snake.game.launcher;

import com.borlehandro.networks.snake.game.api.AbstractClientSession;
import com.borlehandro.networks.snake.game.session.ClientSession;
import com.borlehandro.networks.snake.ui.ServerListController;

import java.io.IOException;

public class ClientLauncher {
    public static AbstractClientSession launch(int port, ServerListController controller) throws IOException {
        ClientSession session = new ClientSession();
        if(controller != null) {
            session.start(controller::onServerListUpdate, port);
        } else session.start(((integer, item) -> {}), port);
        return session;
    }
}

// join 0 Test