package com.borlehandro.networks.snake.ui;

import com.borlehandro.networks.snake.game_controll.MoveController;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.Snake;
import com.borlehandro.networks.snake.spawn.FoodSpawner;
import com.borlehandro.networks.snake.spawn.SnakeSpawner;
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
    private static Map<Integer, Snake> snakes = new HashMap<>();

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

    public static void main(String[] args) {
        // launch();

        // Todo move it into Launcher
        Field field = new Field(9);
        FoodSpawner foodSpawner = new FoodSpawner(field);
        MoveController controller = new MoveController(field);
        SnakeSpawner snakeSpawner = new SnakeSpawner(field, snakes);

        // Todo Test Eating with looping
        // Todo Check and handle snakes collisions
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            try {
                String command = in.nextLine();
                String[] arguments = command.split(" ");
                switch (arguments[0]) {
                    case "spawn" -> {
                        int id = snakeSpawner.spawnRandom();
                        System.out.println("Snake id: " + id);
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
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Wrong arguments");
            }
        }
    }
}