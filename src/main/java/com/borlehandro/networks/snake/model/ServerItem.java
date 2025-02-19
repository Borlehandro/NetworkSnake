package com.borlehandro.networks.snake.model;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;

public class ServerItem {
    private final SnakesProto.GameConfig config;
    private final List<Player> players;
    private SocketAddress address;

    public ServerItem(SnakesProto.GameConfig config, List<Player> players, SocketAddress address) {
        this.config = config;
        this.players = players;
        this.address = address;
    }

    public SnakesProto.GameConfig getConfig() {
        return config;
    }

    public void changeSocketAddress(SocketAddress address) {
        this.address = address;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "ServerItem{" +
                "config=" + config +
                ", players=" + players +
                ", address=" + address +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerItem)) return false;
        ServerItem that = (ServerItem) o;
        return config.equals(that.config) &&
                players.equals(that.players) &&
                address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, players, address);
    }
}
