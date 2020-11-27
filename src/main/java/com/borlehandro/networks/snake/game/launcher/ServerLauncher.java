package com.borlehandro.networks.snake.game.launcher;

import com.borlehandro.networks.snake.PropertiesLoader;
import com.borlehandro.networks.snake.game.session.ServerSession;
import com.borlehandro.networks.snake.message_handlers.ServerMessagesHandler;
import com.borlehandro.networks.snake.protocol.GameConfig;

import java.io.IOException;

public class ServerLauncher {
    public static ServerSession launch(int port) throws IOException {
        GameConfig config = PropertiesLoader.getInstance().loadGameConfig();
        ServerSession session = new ServerSession(config);
        session.start(port);
        return session;
    }
}
