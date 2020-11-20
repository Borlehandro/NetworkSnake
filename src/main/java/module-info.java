module com.borlehandro.networks.snake {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.borlehandro.networks.snake to javafx.fxml;
    exports com.borlehandro.networks.snake.ui;
}