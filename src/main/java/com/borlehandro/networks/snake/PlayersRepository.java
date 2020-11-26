package com.borlehandro.networks.snake;

import com.borlehandro.networks.snake.protocol.Player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayersRepository {
    private int playersNumber = 0;
    private final Map<Integer, Player> playersMap = new HashMap<>(); // user id - player

    private static PlayersRepository INSTANCE;

    private PlayersRepository() {
    }

    public static PlayersRepository getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlayersRepository();
        return INSTANCE;
    }

    public List<Player> getPlayersCopy() {
        synchronized (playersMap) {
            return List.copyOf(playersMap.values());
        }
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    /**
     * @return New player's id
     */
    public int addPlayer(Player player) {
        int playerId = nextPlayerId();
        synchronized (playersMap) {
            playersMap.put(playerId, player);
        }
        return playerId;
    }

    public SocketAddress findAddressById(int id) {
        var player = playersMap.get(id);
        return new InetSocketAddress(player.getIpAddress(), player.getPort());
    }

    private int nextPlayerId() {
        return playersNumber++;
    }

}
