package com.borlehandro.networks.snake.protocol;

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
}
