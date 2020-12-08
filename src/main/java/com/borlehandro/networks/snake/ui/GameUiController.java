package com.borlehandro.networks.snake.ui;

import com.borlehandro.networks.snake.game.api.Session;
import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Player;
import com.borlehandro.networks.snake.model.Snake;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.stream.Collectors;

import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

public class GameUiController {

    @FXML
    private ListView<String> scoreList;
    @FXML
    private AnchorPane fieldPane;

    private Session session;
    private final Canvas fieldCanvas = new Canvas(300, 300); // Todo fix size
    private final ObservableMap<Integer, Player> players = FXCollections.observableHashMap();

    public void onStateUpdate(Field field, Map<Integer, Player> players) {
        Platform.runLater(() -> {
            this.players.putAll(players);
            scoreList.getItems().setAll(players.values().stream()
                    .map(player -> player.getName() + " : " + player.getScore()).collect(Collectors.toList()));
            drawField(field);
        });
    }

    public void onButtonPressed(KeyEvent keyEvent) {
        System.err.println("============== KEY PRESSED ==============" + keyEvent.getCode());
        Snake.Direction direction = switch (keyEvent.getCode()) {
            case W, UP -> Snake.Direction.UP;
            case D, RIGHT -> Snake.Direction.RIGHT;
            case A, LEFT -> Snake.Direction.LEFT;
            case S, DOWN -> Snake.Direction.DOWN;
            default -> null;
        };
        if (direction != null) {
            session.rotate(direction);
            System.err.println("============== ROTATE ==============");
        }
    }

    private void drawField(Field field) {
        // Test
        var gc = fieldCanvas.getGraphicsContext2D();
        // Todo test clearing
        gc.clearRect(0, 0, fieldCanvas.getWidth(), fieldCanvas.getHeight());
        gc.setStroke(Color.BLACK);

        for (int i = field.getFieldMatrix().length - 1; i >= 0; --i) {
            for (int j = 0; j < field.getFieldMatrix()[i].length; ++j) {
                switch (field.getFieldMatrix()[i][j].getState()) {
                    case EMPTY -> gc.setFill(Color.WHITE);
                    case WITH_SNAKE_HEAD, WITH_SNAKE_BODY -> gc.setFill(getFieldNodeColor(field.getFieldMatrix()[i][j]));
                    case WITH_FOOD -> gc.setFill(Color.RED);
                }
                gc.fillRect(i * 15, (field.getFieldMatrix()[i].length - 1 - j) * 15, 10, 10);
            }
        }
    }

    // Todo test colors
    private Color getFieldNodeColor(FieldNode fieldNode) {
        var body = fieldNode.getBodiesOnTheNode();
        int id = -1;
        Snake s;
        if (!fieldNode.getBodiesOnTheNode().isEmpty()) {
            s = body.iterator().next();
        } else {
            s = fieldNode.getHeadsOnTheNode().iterator().next();
        }
        if (!s.getState().equals(Snake.SnakeState.ZOMBIE))
            id = s.getPlayerId();
        if (id >= 0)
            return Color.rgb((255 - (id * 20)) % 255, 200, (id * 20) % 255);
        else
            return Color.rgb(205, 255, 205);
    }

    public void setSession(Session session) {
        this.session = session;
        // Todo remove
        players.addListener((MapChangeListener<? super Integer, ? super Player>) change -> {
            if (change.wasAdded())
                scoreList.getItems().add(change.getValueAdded().getName() + ":" + change.getValueAdded().getScore());
            if (change.wasRemoved()) {
                System.err.println("REMOVE !!!!!!!!!!!!!!!!!!!!!!!!!");
                scoreList.getItems().remove((int) change.getKey());
            } else {
                System.err.println("CHANGE !!!!!!!!!!!!!!!!!!!!!!!!!");
                scoreList.getItems().remove((int) change.getKey());
                System.err.println(change.getValueAdded().getName() + ":" + change.getValueAdded().getScore());
                scoreList.getItems().add(change.getValueAdded().getName() + ":" + change.getValueAdded().getScore());
            }
        });
        fieldPane.getChildren().add(fieldCanvas);
        // Todo set list view unfocused
        Platform.runLater(() -> fieldPane.requestFocus());
        fieldPane.setOnKeyPressed(keyEvent -> {
            onButtonPressed(keyEvent);
            System.out.println("KEY PRESSED!!!!!!!!!!!!!!!!!!!!!!!!!");
        });
        scoreList.setOnKeyPressed(keyEvent -> {
            System.out.println("PRESS LIST");
        });
        // Todo Test exit
        Platform.runLater(() -> fieldPane.getScene().getWindow().setOnCloseRequest(windowEvent -> {
            if (windowEvent.getEventType().equals(WINDOW_CLOSE_REQUEST)) {
                session.exit();
            }
        }));
        // test
        // fieldPane.getChildren().add(new Canvas(100, 100));
    }

    public void changeSession(Session session) {
        this.session = session;
    }
}
