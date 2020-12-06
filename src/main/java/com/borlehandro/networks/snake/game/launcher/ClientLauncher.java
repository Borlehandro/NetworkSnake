package com.borlehandro.networks.snake.game.launcher;

import com.borlehandro.networks.snake.ConsoleController;
import com.borlehandro.networks.snake.game.session.ClientSession;

import java.io.IOException;

public class ClientLauncher {
    public static ClientSession launch(int port) throws IOException {
        ClientSession session = new ClientSession();
        session.start((serverItem -> {}), port);
        return session;
    }
}
