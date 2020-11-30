package com.borlehandro.networks.snake.ui;

import com.borlehandro.networks.snake.PropertiesLoader;
import com.borlehandro.networks.snake.game.MoveController;
import com.borlehandro.networks.snake.game.SnakesCollisionController;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.game.spawn.FoodSpawner;
import com.borlehandro.networks.snake.game.spawn.SnakeSpawner;
import com.borlehandro.networks.snake.protocol.GameConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) throws IOException {
        // launch();

        // Todo move it into Launcher
        // Todo Use players' id here
        Map<Integer, Snake> snakes = new HashMap<>();
        Field field = new Field(9, 9);
        FoodSpawner foodSpawner = new FoodSpawner(field, PropertiesLoader.getInstance().loadGameConfig());
        MoveController controller = new MoveController(field);
        SnakeSpawner snakeSpawner = new SnakeSpawner(field, snakes);
        // O_o
        SnakesCollisionController handler = new SnakesCollisionController(field, snakes.values(), GameConfig.builder().build());

        // Todo Test Eating with looping
        // Todo Check and handle snakes collisions
        // For tests

        snakeSpawner.spawnSnakeByCoordinates(4, 4, Snake.Direction.LEFT, 0); // 1
        snakeSpawner.spawnSnakeByCoordinates(5, 5, Snake.Direction.DOWN, 0); // 2
        snakeSpawner.spawnSnakeByCoordinates(4, 3, Snake.Direction.UP, 0); // 3
        // move 2
        // move 3
        // handle collisions
        // Expect crash all

        // End for tests
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            try {
                String command = in.nextLine();
                String[] arguments = command.split(" ");
                switch (arguments[0]) {
                    case "spawn" -> {
                        snakeSpawner.spawnRandom(0);
                    }
                    case "rotate" -> {
                        Snake s = snakes.get(Integer.parseInt(arguments[2]));
                        switch (arguments[1]) {
                            case "up" -> controller.rotate(s, Snake.Direction.UP);
                            case "down" -> controller.rotate(s, Snake.Direction.DOWN);
                            case "right" -> controller.rotate(s, Snake.Direction.RIGHT);
                            case "left" -> controller.rotate(s, Snake.Direction.LEFT);
                            default -> System.out.println("Wrong direction");
                        }
                    }
                    case "move" -> {
                        Snake s = snakes.get(Integer.parseInt(arguments[1]));
                        controller.moveForward(s);
                    }
                    case "food" -> {
                        // foodSpawner.spawnFoodByCoordinates(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
                        foodSpawner.spawnRandom();
                    }
                    case "handle" -> {
                        handler.controlCollisions(System.out::println);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Wrong arguments");
            }
        }
    }
}