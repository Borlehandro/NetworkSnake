package com.borlehandro.networks.snake;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.io.IOException;
import java.util.Properties;

/**
 * Singleton
 */
public class PropertiesLoader {
    private static final String DEFAULT_PROPERTIES_NAME = "config.properties";
    private static PropertiesLoader INSTANCE;
    private Properties properties = new Properties();

    private PropertiesLoader() throws IOException {
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_NAME));
    }

    public static PropertiesLoader getInstance() throws IOException {
        if(INSTANCE == null) {
            INSTANCE = new PropertiesLoader();
        }
        return INSTANCE;
    }

    public SnakesProto.GameConfig loadGameConfig() {
        return SnakesProto.GameConfig
                .newBuilder()
                .setWidth(Integer.parseInt(properties.getOrDefault("width", 40).toString()))
                .setHeight(Integer.parseInt(properties.getOrDefault("height", 40).toString()))
                .setFoodStatic(Integer.parseInt(properties.getOrDefault("food_static", 1).toString()))
                .setFoodPerPlayer(Integer.parseInt(properties.getOrDefault("food_per_player", 1).toString()))
                .setStateDelayMs(Integer.parseInt(properties.getOrDefault("state_delay_ms", 1000).toString()))
                .setDeadFoodProb(Float.parseFloat(properties.getOrDefault("dead_food_prob", 0.1).toString()))
                .setPingDelayMs(Integer.parseInt(properties.getOrDefault("ping_delay_ms", 100).toString()))
                .setNodeTimeoutMs(Integer.parseInt(properties.getOrDefault("node_timeout_millis", 800).toString()))
                .build();
    }

}
