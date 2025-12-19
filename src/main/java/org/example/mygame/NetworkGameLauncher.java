package org.example.mygame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.mygame.fx.controller.NetworkGameController;
import org.example.mygame.fx.view.MinerRaceView;
import org.example.mygame.fx.view.NetworkLobbyView;
import org.example.mygame.udp.Server;

public class NetworkGameLauncher extends Application {
    private Server server;
    private NetworkGameController networkController;
    private NetworkLobbyView lobbyView;
    
    @Override
    public void start(Stage primaryStage) {
        lobbyView = new NetworkLobbyView(primaryStage, 
            (bindIp, port) -> startServer(primaryStage, bindIp, port),
            (host, port) -> connectAsClient(primaryStage, host, port)
        );
        
        lobbyView.showMainMenu();
        
        primaryStage.setOnCloseRequest(e -> {
            stopAll();
            Platform.exit();
            System.exit(0);
        });
    }
    
    private void stopAll() {
        if (networkController != null) {
            networkController.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    private void startServer(Stage primaryStage, String bindIp, int port) {
        try {
            server = new Server(port, bindIp);
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    Platform.runLater(() -> 
                        lobbyView.showAlert("Ошибка сервера", "Не удалось запустить сервер: " + e.getMessage()));
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            Platform.runLater(() -> connectAsClient(primaryStage, "localhost", port));
            
        } catch (Exception e) {
            lobbyView.showAlert("Ошибка", "Не удалось создать сервер: " + e.getMessage());
        }
    }
    
    private void connectAsClient(Stage primaryStage, String host, int port) {
        networkController = new NetworkGameController();
        networkController.connect(host, port);
        
        MinerRaceView view = new MinerRaceView(networkController);
        Scene scene = new Scene(view);

        primaryStage.setTitle("Гонка Шахтеров - Сетевой режим");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);

        Platform.runLater(() -> {
            view.requestFocus();
            view.start();
        });

        primaryStage.setOnShown(e -> {
            view.requestFocus();
            view.start();
        });
        
        primaryStage.setOnCloseRequest(e -> {
            view.stop();
            stopAll();
            Platform.exit();
            System.exit(0);
        });
        
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
