package com.borlehandro.networks.snake.game_controll;

import java.io.IOException;
import java.util.Properties;

public class EventsController {
    private final static Properties properties = System.getProperties();

    public static void startGame() {
        try {
            loadProperties();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void joinGame() {

    }

    private static void loadProperties() throws IOException {
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
    }
}
