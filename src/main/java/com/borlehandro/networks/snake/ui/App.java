package com.borlehandro.networks.snake.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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