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
        scene = new Scene(loadFXML("main_menu"), 360, 360);
        stage.setScene(scene);
        stage.show();
    }

    private Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(fxml + ".fxml"));
        MenuController controller = fxmlLoader.getController();
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}