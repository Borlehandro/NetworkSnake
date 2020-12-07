module com.borlehandro.networks.snake {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    // Todo refactor
    opens com.borlehandro.networks.snake.ui to javafx.fxml;
    opens com.borlehandro.networks.snake.model to com.google.gson;
    opens com.borlehandro.networks.snake.messages to com.google.gson;
    opens com.borlehandro.networks.snake.messages.state to com.google.gson;
    opens com.borlehandro.networks.snake.messages.action to com.google.gson;
    exports com.borlehandro.networks.snake.ui;
    exports com.borlehandro.networks.snake.model;
    exports com.borlehandro.networks.snake.game.api;
}