package org.example.mygame.fx.view.input;

import javafx.scene.input.KeyCode;
import org.example.mygame.fx.controller.IGameController;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.model.Miner;

import java.util.Set;

public class LocalInputHandler implements IInputHandler {
    private final IGameController controller;

    public LocalInputHandler(IGameController controller) {
        this.controller = controller;
    }

    @Override
    public void handleInput(Set<KeyCode> pressedKeys, Set<KeyCode> keysJustPressed, double deltaTime) {
        GameState gameState = controller.getGameState();
        if (gameState == null) return;

        if (pressedKeys.contains(KeyCode.A)) {
            if (keysJustPressed.contains(KeyCode.A)) {
                gameState.turnMiner(1, Miner.Direction.LEFT);
            }
            gameState.mineBlock(1, deltaTime);
        } else if (pressedKeys.contains(KeyCode.D)) {
            if (keysJustPressed.contains(KeyCode.D)) {
                gameState.turnMiner(1, Miner.Direction.RIGHT);
            }
            gameState.mineBlock(1, deltaTime);
        } else if (pressedKeys.contains(KeyCode.S)) {
            if (keysJustPressed.contains(KeyCode.S)) {
                gameState.turnMiner(1, Miner.Direction.DOWN);
            }
            gameState.mineBlock(1, deltaTime);
        }
        
        if (pressedKeys.contains(KeyCode.W)) {
            gameState.moveMinerInDirection(1, deltaTime);
        }

        if (pressedKeys.contains(KeyCode.LEFT)) {
            if (keysJustPressed.contains(KeyCode.LEFT)) {
                gameState.turnMiner(2, Miner.Direction.LEFT);
            }
            gameState.mineBlock(2, deltaTime);
        } else if (pressedKeys.contains(KeyCode.RIGHT)) {
            if (keysJustPressed.contains(KeyCode.RIGHT)) {
                gameState.turnMiner(2, Miner.Direction.RIGHT);
            }
            gameState.mineBlock(2, deltaTime);
        } else if (pressedKeys.contains(KeyCode.DOWN)) {
            if (keysJustPressed.contains(KeyCode.DOWN)) {
                gameState.turnMiner(2, Miner.Direction.DOWN);
            }
            gameState.mineBlock(2, deltaTime);
        }
        
        if (pressedKeys.contains(KeyCode.UP)) {
            gameState.moveMinerInDirection(2, deltaTime);
        }
    }

    @Override
    public void reset() {
    }
}
