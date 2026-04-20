module org.example.dprojectilegame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.desktop;

    opens org.example.dprojectilegame to javafx.fxml;
    exports org.example.dprojectilegame;
}