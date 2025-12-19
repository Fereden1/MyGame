package org.example.mygame.fx.view.input;

import javafx.scene.input.KeyCode;
import java.util.Set;

public interface IInputHandler {
    void handleInput(Set<KeyCode> pressedKeys, Set<KeyCode> keysJustPressed, double deltaTime);
    void reset();
}
