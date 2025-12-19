package org.example.mygame.fx.view.renderer;

import javafx.scene.canvas.GraphicsContext;
import org.example.mygame.fx.model.GameState;

public interface IRenderer {
    void render(GraphicsContext gc, GameState gameState, double width, double height);
    void cleanup();
}

