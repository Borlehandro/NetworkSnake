package com.borlehandro.networks.snake.game.repository;

import com.borlehandro.networks.snake.model.ServerItem;
import com.borlehandro.networks.snake.protocol.Player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class PlayersServersRepository {
    private int playersNumber = 0;
    private int serversNumber = 0;
    private final Map<Integer, Player> playersMap = new HashMap<>(); // user id - player
    private final Map<Integer, Long> lastReceivedMessageTimeMillis = new HashMap<>(); // id - last received message time
    private final Map<Integer, Long> lastSentMessageTimeMillis = new HashMap<>(); // id - last sent message time
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

    public Collection<Player> getPlayers() {
        synchronized (playersMap) {
            return playersMap.values();
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

    public void putPlayer(Player player) {
        synchronized (playersMap) {
            playersMap.put(player.getId(), player);
        }
    }

    public int addServerItem(ServerItem serverItem) {
        int id = -1;
        synchronized (serversMap) {
            if (!serversMap.containsValue(serverItem)) {
                id = nextServerId();
                serversMap.put(id, serverItem);
            }
            return id;
        }
    }

    // Todo test optional
    public synchronized Optional<SocketAddress> findPlayerAddressById(int id) {
        var player = playersMap.get(id);
        if(player != null) {
            return Optional.of(new InetSocketAddress(player.getIpAddress(), player.getPort()));
        }
        return Optional.empty();
    }

    public void updateLastReceivedMessageTimeMillis(int id, long millis, boolean isNew) {
        synchronized (lastReceivedMessageTimeMillis) {
            lastReceivedMessageTimeMillis.put(id, millis);
        }
    }

    public void updateLastSentMessageTimeMillis(int id, long millis, boolean isNew) {
        synchronized (lastSentMessageTimeMillis) {
            // Todo test replace.
            //  Before this action you should add id into this map.
            lastSentMessageTimeMillis.replace(id, millis);
        }
    }

    public void updateAllSentMessageTimes(long millis) {
        synchronized (lastSentMessageTimeMillis) {
            // Todo test replace.
            //  Before this action you should add id into this map.
            //  It's too long.
            for (Map.Entry<Integer, Long> entry : lastSentMessageTimeMillis.entrySet()) {
                entry.setValue(millis);
            }
        }
    }

    public Map<Integer, Long> getLastReceivedMessageTimeMillis() {
        return lastReceivedMessageTimeMillis;
    }

    public Map<Integer, Long> getLastSentMessageTimeMillis() {
        return lastSentMessageTimeMillis;
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

    public void removePlayer(int id) {
        playersMap.remove(id);
        lastReceivedMessageTimeMillis.remove(id);
    }

    public ServerItem acceptServer() {
        currentServerId = serverToConnectId;
        serverToConnectId = -1;
        return serversMap.get(currentServerId);
    }

}
