package org.example.mygame.fx.controller;

import org.example.mygame.fx.model.GameState;

public interface IGameController {
    GameState getGameState();
    boolean isRunning();
    boolean isGameOver();
    String getFormattedGameTime();
    double getGameTime();
    void update();
    void stop();
    void start();
    void restart();
    default int getPlayerId() { return -1; }
    default boolean isConnected() { return false; }
    default boolean isReadyToStart() { return false; }
}
