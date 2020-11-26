module com.borlehandro.networks.snake {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    // Todo refactor
    opens com.borlehandro.networks.snake.ui to javafx.fxml;
    opens com.borlehandro.networks.snake.model to com.google.gson;
    opens com.borlehandro.networks.snake.protocol to com.google.gson;
    opens com.borlehandro.networks.snake.protocol.messages to com.google.gson;
    opens com.borlehandro.networks.snake.protocol.messages.state to com.google.gson;
    opens com.borlehandro.networks.snake.protocol.messages.action to com.google.gson;
    exports com.borlehandro.networks.snake.ui;
}