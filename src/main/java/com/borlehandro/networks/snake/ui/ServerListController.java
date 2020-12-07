package com.borlehandro.networks.snake.ui;

import com.borlehandro.networks.snake.game.api.AbstractClientSession;
import com.borlehandro.networks.snake.model.ServerItem;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerListController {

    @FXML
    private ListView<String> serverListView;
    private AbstractClientSession clientSession;
    private final ObservableMap<Integer, String> serverIds = FXCollections.observableHashMap();

    public void onServerListUpdate(int id, ServerItem item) {
        serverIds.put(id, item.toString());
    }

    public void setClientSession(AbstractClientSession clientSession) {
        this.clientSession = clientSession;
        serverIds.addListener((MapChangeListener.Change<? extends Integer, ? extends String> c) -> {
            if (c.wasAdded()) {
                serverListView.getItems().add(c.getValueAdded());
            }
            if (c.wasRemoved()) {
                serverListView.getItems().remove(c.getValueRemoved());
            }
        });
    }

    public void onItemClick() {
        // Todo fix name
        clientSession.joinGame(serverListView.getSelectionModel().getSelectedIndex(), "UiTest");
        try {
            // Launch game window
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("game_layout.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 720, 360));
            var gameController = (GameUiController) loader.getController();
            clientSession.setController(gameController);
            gameController.setSession(clientSession);
            serverListView.getScene().getWindow().hide();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}