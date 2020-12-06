package com.borlehandro.networks.snake.ui;

import com.borlehandro.networks.snake.game.api.AbstractClientSession;
import com.borlehandro.networks.snake.model.ServerItem;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

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
    }

}