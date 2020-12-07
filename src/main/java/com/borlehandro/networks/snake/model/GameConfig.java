package com.borlehandro.networks.snake.model;

import java.util.Objects;

public class GameConfig {
    private int fieldWidth = 40;
    private int fieldHeight = 40;
    private int foodStatic = 1;
    private int foodPerPlayer = 1;
    private int stateDelayMillis = 1000;
    private int pingDelayMillis = 100;
    private double deadFoodProb = 0.1;
    private int nodeTimeoutMillis = 800;

    private GameConfig() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GameConfig INSTANCE = new GameConfig();

        private Builder(){}

        public GameConfig build() {
            return INSTANCE;
        }

        public Builder withWidth(int width) {
            INSTANCE.fieldWidth = width;
            return this;
        }

        public Builder withHeight(int height) {
            INSTANCE.fieldHeight = height;
            return this;
        }

        public Builder withFoodStatic(int foodStatic) {
            INSTANCE.foodStatic = foodStatic;
            return this;
        }

        public Builder withFoodPerPlayer(int foodPerPlayer) {
            INSTANCE.foodPerPlayer = foodPerPlayer;
            return this;
        }

        public Builder withStateDelayMillis(int stateDelayMillis) {
            INSTANCE.stateDelayMillis = stateDelayMillis;
            return this;
        }

        public Builder withDeadFoodProb(double deadFoodProb) {
            INSTANCE.deadFoodProb = deadFoodProb;
            return this;
        }

        public Builder withPingDelayMillis(int pingDelayMillis) {
            INSTANCE.pingDelayMillis = pingDelayMillis;
            return this;
        }

        public Builder withNodeTimeoutMillis(int nodeTimeoutMillis) {
            INSTANCE.nodeTimeoutMillis = nodeTimeoutMillis;
            return this;
        }
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public int getFoodPerPlayer() {
        return foodPerPlayer;
    }

    public int getStateDelayMillis() {
        return stateDelayMillis;
    }

    public int getPingDelayMillis() {
        return pingDelayMillis;
    }

    public double getDeadFoodProb() {
        return deadFoodProb;
    }

    public int getNodeTimeoutMillis() {
        return nodeTimeoutMillis;
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "fieldWidth=" + fieldWidth +
                ", fieldHeight=" + fieldHeight +
                ", foodStatic=" + foodStatic +
                ", foodPerPlayer=" + foodPerPlayer +
                ", stateDelayMillis=" + stateDelayMillis +
                ", pingDelayMillis=" + pingDelayMillis +
                ", deadFoodProb=" + deadFoodProb +
                ", nodeTimeoutMillis=" + nodeTimeoutMillis +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameConfig)) return false;
        GameConfig config = (GameConfig) o;
        return fieldWidth == config.fieldWidth &&
                fieldHeight == config.fieldHeight &&
                foodStatic == config.foodStatic &&
                foodPerPlayer == config.foodPerPlayer &&
                stateDelayMillis == config.stateDelayMillis &&
                pingDelayMillis == config.pingDelayMillis &&
                Double.compare(config.deadFoodProb, deadFoodProb) == 0 &&
                nodeTimeoutMillis == config.nodeTimeoutMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldWidth, fieldHeight, foodStatic, foodPerPlayer, stateDelayMillis, pingDelayMillis, deadFoodProb, nodeTimeoutMillis);
    }
}
