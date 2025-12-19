package org.example.mygame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.mygame.fx.controller.GameController;
import org.example.mygame.fx.view.MinerRaceView;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        showMainMenu(primaryStage);
    }
    
    private void showMainMenu(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0f7fa, #80deea);");
        
        Label title = new Label("Гонка Шахтеров");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #006064; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        
        Button singlePlayerBtn = createStyledButton("Одиночная игра");
        Button networkGameBtn = createStyledButton("Сетевая игра");
        Button exitBtn = createStyledButton("Выход");
        exitBtn.setStyle("-fx-font-size: 16px; -fx-base: #ff5252; -fx-text-fill: white; -fx-background-radius: 5;");
        
        singlePlayerBtn.setOnAction(e -> startSinglePlayer(stage));
        networkGameBtn.setOnAction(e -> startNetworkGame(stage));
        exitBtn.setOnAction(e -> {
            stage.close();
            System.exit(0);
        });
        
        root.getChildren().addAll(title, singlePlayerBtn, networkGameBtn, exitBtn);
        
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Гонка Шахтеров - Главное меню");
        stage.setScene(scene);
        stage.show();
    }
    
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-font-size: 16px; -fx-base: #0097a7; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }
    
    private void startSinglePlayer(Stage stage) {
        GameController controller = new GameController();
        MinerRaceView view = new MinerRaceView(controller);
        
        Scene scene = new Scene(view);
        stage.setScene(scene);
        stage.setMaximized(true);

        view.start();
        
        stage.setOnCloseRequest(e -> {
            view.stop();
            System.exit(0);
        });
    }
    
    private void startNetworkGame(Stage stage) {
        NetworkGameLauncher networkLauncher = new NetworkGameLauncher();
        try {
            networkLauncher.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
