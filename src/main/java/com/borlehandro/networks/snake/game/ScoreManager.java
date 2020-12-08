package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.model.Player;

public class ScoreManager {

    private final PlayersServersRepository repository = PlayersServersRepository.getInstance();

    private static ScoreManager instance;

    private ScoreManager() {
    }

    public static ScoreManager getInstance() {
        if (instance == null)
            instance = new ScoreManager();
        return instance;
    }

    public void incrementScore(int playerId) {
        synchronized (repository) {
            Player player = repository.getPlayersMap().get(playerId);
            if (player != null) {
                System.err.println("INCREMENT " + playerId);
                player.setScore(player.getScore() + 1);
            }
        }
    }

}