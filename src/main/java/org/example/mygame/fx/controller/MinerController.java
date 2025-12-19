package org.example.mygame.fx.controller;

import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.model.Miner;

public class MinerController {
    private GameController gameController;
    
    public MinerController(GameController gameController) {
        this.gameController = gameController;
    }

    public void moveMinerLeft(int minerId, double deltaTime) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        gameController.getGameState().moveMinerLeft(minerId, deltaTime);
    }

    public void moveMinerRight(int minerId, double deltaTime) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        gameController.getGameState().moveMinerRight(minerId, deltaTime);
    }

    public void mineDown(int minerId, double deltaTime) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        GameState gameState = gameController.getGameState();
        gameState.turnMiner(minerId, Miner.Direction.DOWN);
        gameState.mineBlock(minerId, deltaTime);
    }

    public void turnMinerLeft(int minerId) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        gameController.getGameState().turnMiner(minerId, Miner.Direction.LEFT);
    }

    public void turnMinerRight(int minerId) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        gameController.getGameState().turnMiner(minerId, Miner.Direction.RIGHT);
    }

    public void turnMinerDown(int minerId) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        gameController.getGameState().turnMiner(minerId, Miner.Direction.DOWN);
    }

    public void mineBlock(int minerId, double deltaTime) {
        if (gameController.isGameOver() || !gameController.isRunning()) {
            return;
        }
        gameController.getGameState().mineBlock(minerId, deltaTime);
    }

    public GameController getGameController() {
        return gameController;
    }
    
    public GameState getGameState() {
        return gameController.getGameState();
    }
}

