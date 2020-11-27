package com.borlehandro.networks.snake.protocol;

import java.util.Objects;
import java.util.Optional;

public class Player {
    private final String name;
    // Set when add
    private int id;
    private final String ipAddress;
    private final int port;
    // Set when add
    private NodeRole role;
    private int score;

    private Player(String name,
                   String ipAddress,
                   int port) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    public static class Builder {

        private String name;
        private int id = -1;
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
            if (name != null && ipAddress != null && port > 0) {
                var player = new Player(name, ipAddress, port);
                player.setScore(score);
                return Optional.of(player);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", role=" + role +
                ", score=" + score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}