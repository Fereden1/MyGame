module org.example.mygame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens org.example.mygame to javafx.fxml;
    exports org.example.mygame;
    exports org.example.mygame.fx.view;
    exports org.example.mygame.fx.view.renderer;
    exports org.example.mygame.fx.view.input;
    exports org.example.mygame.fx.view.ui;
    exports org.example.mygame.fx.controller;
    exports org.example.mygame.fx.model;
    exports org.example.mygame.udp;
}