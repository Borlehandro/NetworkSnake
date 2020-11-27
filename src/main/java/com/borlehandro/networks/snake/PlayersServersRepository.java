package com.borlehandro.networks.snake;

import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.protocol.Player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayersServersRepository {
    private int playersNumber = 0;
    private int serversNumber = 0;
    private final Map<Integer, Player> playersMap = new HashMap<>(); // user id - player
    private final Map<Integer, ServerItem> serversMap = new HashMap<>(); // server id - socket address
    private int serverToConnectId = -1;
    private int currentServerId = -1;

    private static PlayersServersRepository INSTANCE;

    private PlayersServersRepository() {
    }

    public static PlayersServersRepository getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlayersServersRepository();
        return INSTANCE;
    }

    public List<Player> getPlayersCopy() {
        synchronized (playersMap) {
            return List.copyOf(playersMap.values());
        }
    }

    public List<ServerItem> getServersCopy() {
        synchronized (serversMap) {
            return List.copyOf(serversMap.values());
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

    public int addServerItem(ServerItem serverItem) {
        int id = -1;
        synchronized (serversMap) {
            if(!serversMap.containsValue(serverItem)) {
                id = nextServerId();
                serversMap.put(id, serverItem);
            }
            return id;
        }
    }

    public SocketAddress findPlayerAddressById(int id) {
        var player = playersMap.get(id);
        return new InetSocketAddress(player.getIpAddress(), player.getPort());
    }

    public SocketAddress findServerSocketAddressById(int id) {
        return serversMap.get(id).getAddress();
    }

    private int nextPlayerId() {
        return playersNumber++;
    }

    private int nextServerId() {
        return serversNumber++;
    }

    public int getCurrentServerId() {
        return currentServerId;
    }

    public void setServerToConnectId(int id) {
        serverToConnectId = id;
    }

    public ServerItem acceptServer() {
        currentServerId = serverToConnectId;
        serverToConnectId = -1;
        return serversMap.get(currentServerId);
    }

}
