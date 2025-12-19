package org.example.mygame.fx.controller;

import javafx.application.Platform;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.udp.Client;
import org.example.mygame.udp.NetworkConstants;

public class NetworkGameController implements IGameController {
    private Client client;
    private GameState gameState;
    
    private boolean isRunning = false;
    private boolean isReadyToStart = false;
    
    private double gameTime;
    private String formattedGameTime;
    private long lastUpdateTime;
    
    public NetworkGameController() {
        this.gameState = new GameState();
        this.isRunning = false;
        this.gameTime = 0.0;
        this.formattedGameTime = "00:00";
        this.lastUpdateTime = 0;
    }
    
    public void connect(String host, int port) {
        client = new Client();
        client.setStateUpdateCallback(this::onStateUpdate);
        client.setMessageCallback(this::onMessage);
        client.connect(host, port);
    }
    
    private void onStateUpdate(GameState state) {
        Platform.runLater(() -> {
            if (state != null) {
                boolean wasGameOver = this.gameState != null && this.gameState.isGameOver();
                boolean isNowGameOver = state.isGameOver();
                
                this.gameState = state;

                if (!wasGameOver && isNowGameOver) {
                    System.out.println("Game over detected on client! Winner: " + 
                        (state.getWinner() != null ? state.getWinner().getName() : "null"));
                    isRunning = false;
                }
            }
        });
    }
    
    public void sendStartGame() {
        if (client != null && client.isConnected()) {
            System.out.println("Sending CMD:START...");
            client.sendCommand(NetworkConstants.CMD_START);
        }
    }

    private void onMessage(String message) {
        Platform.runLater(() -> processMessage(message));
    }
    
    private void processMessage(String message) {
        System.out.println("Processing message: " + message);
        
        if (message.equals(NetworkConstants.MSG_READY_TO_START)) {
            isReadyToStart = true;
        } else if (message.equals(NetworkConstants.MSG_GAME_START)) {
            isReadyToStart = false;
            isRunning = true;
            gameTime = 0.0;
            lastUpdateTime = System.nanoTime();
            System.out.println("GAME STARTED/RESTARTED! isRunning set to true. lastUpdateTime=" + lastUpdateTime);
        } else if (message.startsWith(NetworkConstants.PREFIX_PLAYER_ID)) {
        }
    }
    
    public void sendInput(String command, double deltaTime) {
        if (client != null && client.isConnected()) {
            client.sendInput(command, deltaTime);
        } else {
            System.out.println("Cannot send input: not connected");
        }
    }
    
    public void update() {
        long currentTime = System.nanoTime();
        
        if (!isRunning) {
            return;
        }

        if (isGameOver()) {
            return;
        }

        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTime;
            return;
        }
        
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = currentTime;
        
        if (deltaTime < 0) deltaTime = 0;
        if (deltaTime > 0.1) deltaTime = 0.1;
        
        gameTime += deltaTime;
        
        int minutes = (int) (gameTime / 60);
        int seconds = (int) (gameTime % 60);
        String newTime = String.format("%02d:%02d", minutes, seconds);
        
        if (!newTime.equals(formattedGameTime)) {
             formattedGameTime = newTime;
        }
    }
    
    public GameState getGameState() {
            return gameState;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isGameOver() {
            return gameState != null && gameState.isGameOver();
    }
    
    public String getFormattedGameTime() {
        return formattedGameTime;
    }
    
    public double getGameTime() {
        return gameTime;
    }
    
    public void stop() {
        isRunning = false;
        if (client != null) {
            client.stop();
        }
    }
    
    public void start() {}
    public void restart() {}
    
    public int getPlayerId() {
        return client != null ? client.getPlayerId() : -1;
    }
    
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
    public boolean isReadyToStart() {
        return isReadyToStart;
    }
}
