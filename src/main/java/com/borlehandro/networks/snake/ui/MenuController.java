package com.borlehandro.networks.snake.ui;

import com.borlehandro.networks.snake.game.api.AbstractClientSession;
import com.borlehandro.networks.snake.game.api.Session;
import com.borlehandro.networks.snake.game.launcher.ClientLauncher;
import com.borlehandro.networks.snake.game.launcher.ServerLauncher;
import com.borlehandro.networks.snake.game.session.ServerSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    @FXML
    private VBox menuBox;

    public void onStartServerClick() {
        System.out.println("start server");
        // Todo fix port
        try {
            Session session = ServerLauncher.launch(8080);
            // Launch game window
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("game_layout.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 720, 360));
            var gameController = (GameUiController) loader.getController();
            session.setController(gameController);
            gameController.setSession(session);
            menuBox.getScene().getWindow().hide();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onConnectClick() {
        System.out.println("connect");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("server_list.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 360, 360));
            // Todo fix port
            var serverListController = (ServerListController)loader.getController();
            AbstractClientSession session = ClientLauncher.launch(8081, serverListController);
            serverListController.setClientSession(session);
            stage.show();
            menuBox.getScene().getWindow().hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onExitClick() {
        System.out.println("exit");
        Stage stage = (Stage) menuBox.getScene().getWindow();
        stage.close();
    }
}