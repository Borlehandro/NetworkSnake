package com.borlehandro.networks.snake;

import com.borlehandro.networks.snake.game.api.AbstractController;
import com.borlehandro.networks.snake.game.api.Session;
import com.borlehandro.networks.snake.model.Snake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleController extends Thread implements AbstractController {

    private Session session;

    public ConsoleController(Session session) {
        this.session = session;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.err.println("START");
            while (!interrupted()) {
                String s = reader.readLine();
                Snake.Direction direction;
                switch (s) {
                    case "w" -> direction = Snake.Direction.UP;
                    case "a" -> direction = Snake.Direction.LEFT;
                    case "s" -> direction = Snake.Direction.DOWN;
                    case "d" -> direction = Snake.Direction.RIGHT;
                    case "exit" -> {
                        session.exit();
                        continue;
                    }
                    default -> {
                        continue;
                    }
                }
                session.rotate(direction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("STOP");
    }

    public void changeSession(Session session) {
        this.session = session;
    }
}