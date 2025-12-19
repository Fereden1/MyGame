package org.example.mygame.fx.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.mygame.udp.NetworkConstants;

import java.net.InetAddress;
import java.util.function.BiConsumer;

public class NetworkLobbyView {
    
    private final Stage stage;
    private final BiConsumer<String, Integer> onServerStart;
    private final BiConsumer<String, Integer> onClientConnect;
    
    public NetworkLobbyView(Stage stage, 
                          BiConsumer<String, Integer> onServerStart,
                          BiConsumer<String, Integer> onClientConnect) {
        this.stage = stage;
        this.onServerStart = onServerStart;
        this.onClientConnect = onClientConnect;
    }
    
    public void showMainMenu() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0;");
        
        Label title = new Label("Гонка Шахтеров - Сетевой режим");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Button hostButton = createStyledButton("Создать сервер");
        Button joinButton = createStyledButton("Подключиться к серверу");
        
        hostButton.setOnAction(e -> showServerDialog());
        joinButton.setOnAction(e -> showJoinDialog());
        
        root.getChildren().addAll(title, hostButton, joinButton);
        
        Scene scene = new Scene(root, 350, 200);
        stage.setTitle("Меню сетевой игры");
        stage.setScene(scene);
        stage.show();
    }
    
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-font-size: 14px; -fx-base: #4a90e2; -fx-text-fill: white;");
        return btn;
    }
    
    private void showServerDialog() {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        
        Label infoLabel = new Label("Настройки сервера");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label ipLabel = new Label("IP для привязки (по умолчанию 0.0.0.0):");
        TextField ipField = new TextField(NetworkConstants.DEFAULT_BIND_ADDRESS);
        
        Label portLabel = new Label("Порт:");
        TextField portField = new TextField(String.valueOf(NetworkConstants.DEFAULT_PORT));
        
        Button createButton = new Button("Запустить сервер");
        createButton.setMaxWidth(Double.MAX_VALUE);
        
        Label ipInfo = new Label("Ваш локальный IP (сообщите другу):");
        TextField ipDisplay = new TextField();
        ipDisplay.setEditable(false);
        try {
            ipDisplay.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            ipDisplay.setText("Не удалось определить IP");
        }
        
        createButton.setOnAction(e -> {
            try {
                String bindIp = ipField.getText();
                int port = Integer.parseInt(portField.getText());
                dialog.close();
                onServerStart.accept(bindIp, port);
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат порта");
            }
        });
        
        root.getChildren().addAll(infoLabel, ipInfo, ipDisplay, new Separator(), 
                                  ipLabel, ipField, portLabel, portField, createButton);
        
        Scene scene = new Scene(root, 300, 350);
        dialog.setTitle("Создание сервера");
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showJoinDialog() {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        
        Label ipLabel = new Label("IP адрес сервера:");
        TextField ipField = new TextField(NetworkConstants.DEFAULT_HOST);
        
        Label portLabel = new Label("Порт:");
        TextField portField = new TextField(String.valueOf(NetworkConstants.DEFAULT_PORT));
        
        Button connectButton = new Button("Подключиться");
        connectButton.setMaxWidth(Double.MAX_VALUE);
        
        connectButton.setOnAction(e -> {
            try {
                String ip = ipField.getText();
                int port = Integer.parseInt(portField.getText());
                dialog.close();
                onClientConnect.accept(ip, port);
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат порта");
            }
        });
        
        root.getChildren().addAll(ipLabel, ipField, portLabel, portField, connectButton);
        
        Scene scene = new Scene(root, 250, 200);
        dialog.setTitle("Подключение");
        dialog.setScene(scene);
        dialog.show();
    }
    
    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

