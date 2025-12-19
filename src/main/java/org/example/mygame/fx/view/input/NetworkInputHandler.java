package org.example.mygame.fx.view.input;

import javafx.scene.input.KeyCode;
import org.example.mygame.fx.controller.NetworkGameController;
import org.example.mygame.udp.NetworkConstants;
import java.util.Set;

public class NetworkInputHandler implements IInputHandler {
    private final NetworkGameController networkController;

    public NetworkInputHandler(NetworkGameController networkController) {
        this.networkController = networkController;
    }

    @Override
    public void handleInput(Set<KeyCode> pressedKeys, Set<KeyCode> keysJustPressed, double deltaTime) {
        if (!networkController.isConnected()) return;

        int playerId = networkController.getPlayerId();
        if (playerId == -1) return;

        if (playerId == 1) {
            handlePlayer1Input(pressedKeys, keysJustPressed, deltaTime);
        } else if (playerId == 2) {
            handlePlayer2Input(pressedKeys, keysJustPressed, deltaTime);
        }
    }

    private void handlePlayer1Input(Set<KeyCode> pressedKeys, Set<KeyCode> keysJustPressed, double deltaTime) {
        if (pressedKeys.contains(KeyCode.A)) {
            if (keysJustPressed.contains(KeyCode.A)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_LEFT, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        } else if (pressedKeys.contains(KeyCode.D)) {
            if (keysJustPressed.contains(KeyCode.D)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_RIGHT, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        } else if (pressedKeys.contains(KeyCode.S)) {
            if (keysJustPressed.contains(KeyCode.S)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_DOWN, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        }
        
        if (pressedKeys.contains(KeyCode.W)) {
            networkController.sendInput(NetworkConstants.INPUT_MOVE, deltaTime);
        }
    }

    private void handlePlayer2Input(Set<KeyCode> pressedKeys, Set<KeyCode> keysJustPressed, double deltaTime) {
        if (pressedKeys.contains(KeyCode.A)) {
            if (keysJustPressed.contains(KeyCode.A)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_LEFT, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        } else if (pressedKeys.contains(KeyCode.D)) {
            if (keysJustPressed.contains(KeyCode.D)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_RIGHT, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        } else if (pressedKeys.contains(KeyCode.S)) {
            if (keysJustPressed.contains(KeyCode.S)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_DOWN, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        }
        
        if (pressedKeys.contains(KeyCode.W)) {
            networkController.sendInput(NetworkConstants.INPUT_MOVE, deltaTime);
        }

        if (pressedKeys.contains(KeyCode.LEFT)) {
            if (keysJustPressed.contains(KeyCode.LEFT)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_LEFT, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        } else if (pressedKeys.contains(KeyCode.RIGHT)) {
            if (keysJustPressed.contains(KeyCode.RIGHT)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_RIGHT, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        } else if (pressedKeys.contains(KeyCode.DOWN)) {
            if (keysJustPressed.contains(KeyCode.DOWN)) {
                networkController.sendInput(NetworkConstants.INPUT_TURN_DOWN, deltaTime);
            }
            networkController.sendInput(NetworkConstants.INPUT_MINE, deltaTime);
        }
        
        if (pressedKeys.contains(KeyCode.UP)) {
            networkController.sendInput(NetworkConstants.INPUT_MOVE, deltaTime);
        }
    }

    @Override
    public void reset() {
    }
}
