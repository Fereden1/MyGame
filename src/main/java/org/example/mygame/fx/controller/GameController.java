package org.example.mygame.fx.controller;

import org.example.mygame.fx.model.GameState;

public class GameController implements IGameController {
    private GameState gameState;
    private boolean isRunning;
    private long lastUpdateTime;
    private long gameStartTime;
    private double gameTime;
    
    public GameController() {
        this.gameState = new GameState();
        this.isRunning = false;
        this.lastUpdateTime = System.nanoTime();
        this.gameStartTime = 0;
        this.gameTime = 0.0;
    }

    public void start() {
        isRunning = true;
        lastUpdateTime = System.nanoTime();
        gameStartTime = System.nanoTime();
        gameTime = 0.0;
    }

    public void stop() {
        isRunning = false;
    }

    public void restart() {
        this.gameState = new GameState();
        this.isRunning = true;
        this.lastUpdateTime = System.nanoTime();
        this.gameStartTime = System.nanoTime();
        this.gameTime = 0.0;
    }

    public void update() {
        if (!isRunning) {
            return;
        }
        
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // Конвертация в секунды
        lastUpdateTime = currentTime;

        if (deltaTime > 0.1) {
            deltaTime = 0.1;
        }

        if (gameStartTime > 0 && !gameState.isGameOver()) {
            gameTime = (currentTime - gameStartTime) / 1_000_000_000.0;
        }
        
        gameState.update(deltaTime);
    }

    public GameState getGameState() {
        return gameState;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public double getGameTime() {
        return gameTime;
    }

    public String getFormattedGameTime() {
        int minutes = (int) (gameTime / 60);
        int seconds = (int) (gameTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}

