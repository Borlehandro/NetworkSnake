package com.borlehandro.networks.snake.game.launcher;

import com.borlehandro.networks.snake.PropertiesLoader;
import com.borlehandro.networks.snake.protocol.GameConfig;

import java.io.IOException;

public class ServerLauncher {
    public static void launch() throws IOException {
        GameConfig config = PropertiesLoader.getInstance().loadGameConfig();

    }
}
