package com.borlehandro.networks.snake.game.launcher;

import com.borlehandro.networks.snake.PropertiesLoader;
import com.borlehandro.networks.snake.game.session.ServerSession;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.io.IOException;

public class ServerLauncher {
    public static ServerSession launch(int port) throws IOException {
        SnakesProto.GameConfig config = PropertiesLoader.getInstance().loadGameConfig();
        ServerSession session = new ServerSession(config);
        session.start(port);
        return session;
    }
}
