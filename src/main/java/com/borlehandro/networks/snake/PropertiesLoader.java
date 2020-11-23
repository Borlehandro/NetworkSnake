package com.borlehandro.networks.snake;

import com.borlehandro.networks.snake.protocol.GameConfig;

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

    public GameConfig loadGameConfig() {
        return GameConfig
                .builder()
                .withWidth((int)properties.getOrDefault("width", 40))
                .withHeight((int)properties.getOrDefault("height", 40))
                .withFoodStatic((int)properties.getOrDefault("food_static", 1))
                .withFoodPerPlayer((int)properties.getOrDefault("food_per_player", 1))
                .withStateDelayMillis((int)properties.getOrDefault("state_delay_ms", 1000))
                .withDeadFoodProb((double)properties.getOrDefault("dead_food_prob", 0.1))
                .withPingDelayMillis((int)properties.getOrDefault("ping_delay_ms", 100))
                .withNodeTimeoutMillis((int)properties.getOrDefault("food_per_player", 800))
                .build();
    }

}
