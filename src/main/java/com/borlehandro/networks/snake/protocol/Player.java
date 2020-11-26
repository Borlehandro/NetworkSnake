package com.borlehandro.networks.snake.protocol;

import java.util.Optional;

public class Player {
    private final String name;
    private final int id;
    private final String ipAddress;
    private final int port;
    private final NodeRole role;
    private int score;

    private Player(String name,
                   int id,
                   String ipAddress,
                   int port,
                   NodeRole role) {
        this.name = name;
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.role = role;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public NodeRole getRole() {
        return role;
    }

    public int getScore() {
        return score;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private int id;
        private String ipAddress;
        private int port;
        private NodeRole role;
        private int score;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withId(int id) {
            this.id = id;
            return this;
        }

        public Builder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withRole(NodeRole role) {
            this.role = role;
            return this;
        }

        public Builder withStartScore(int score) {
            this.score = score;
            return this;
        }

        public Optional<Player> build() {
            if (name != null && id >= 0 && ipAddress != null && port > 0 && role != null) {
                var player = new Player(name, id, ipAddress, port, role);
                player.setScore(score);
                return Optional.of(player);
            } else {
                return Optional.empty();
            }
        }
    }
}