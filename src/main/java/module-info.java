module com.borlehandro.networks.snake {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires com.google.protobuf;
    // Todo refactor
    opens com.borlehandro.networks.snake.ui to javafx.fxml;
    opens com.borlehandro.networks.snake.model to com.google.gson;

    exports com.borlehandro.networks.snake.ui;
    exports com.borlehandro.networks.snake.model;
    exports com.borlehandro.networks.snake.game.api;
    exports com.borlehandro.networks.snake.protobuf;
}