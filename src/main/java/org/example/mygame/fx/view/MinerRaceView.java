package org.example.mygame.fx.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.example.mygame.fx.controller.IGameController;
import org.example.mygame.fx.controller.NetworkGameController;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.view.input.IInputHandler;
import org.example.mygame.fx.view.input.LocalInputHandler;
import org.example.mygame.fx.view.input.NetworkInputHandler;
import org.example.mygame.fx.view.renderer.HUDRenderer;
import org.example.mygame.fx.view.renderer.IRenderer;
import org.example.mygame.fx.view.renderer.MinerRaceRenderer;
import org.example.mygame.fx.view.ui.IUIManager;
import org.example.mygame.fx.view.ui.JavaFXUIManager;

import java.util.HashSet;
import java.util.Set;

public class MinerRaceView extends BorderPane {
    private double canvasWidth = 600;
    private double canvasHeight = 800;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private IGameController controller;
    private NetworkGameController networkController;
    private boolean isNetworkMode = false;
    private AnimationTimer gameLoop;

    private IRenderer gameRenderer;
    private IRenderer hudRenderer;
    private IInputHandler inputHandler;
    private IUIManager uiManager;

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Set<KeyCode> keysJustPressed = new HashSet<>();

    private long gameOverTime;
    private static final long RESTART_DELAY_MS = 3000;
    
    public MinerRaceView(IGameController controller) {
        this.controller = controller;
        if (controller instanceof NetworkGameController) {
            this.networkController = (NetworkGameController) controller;
            this.isNetworkMode = true;
            this.inputHandler = new NetworkInputHandler(networkController);
        } else {
            this.inputHandler = new LocalInputHandler(controller);
        }
        
        this.gameOverTime = 0;
        
        initializeUI();

        this.gameRenderer = new MinerRaceRenderer();
        this.hudRenderer = new HUDRenderer(controller);
        
        setupGameLoop();
        setupControls();
    }
    
    private void initializeUI() {
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty().subtract(100));

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                canvasWidth = newVal.doubleValue();
                render();
            }
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                canvasHeight = newVal.doubleValue();
                render();
            }
        });
        
        canvasWidth = 600;
        canvasHeight = 800;
        
        VBox infoPanel = new VBox(10);
        infoPanel.setStyle("-fx-padding: 10;");

        this.uiManager = new JavaFXUIManager(infoPanel);
        
        setCenter(canvas);
        setBottom(infoPanel);
    }
    
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;
            
            @Override
            public void handle(long now) {
                try {
                    double deltaTime = 0.016;
                    if (lastTime > 0) {
                        deltaTime = (now - lastTime) / 1_000_000_000.0;
                    }
                    lastTime = now;
                    if (deltaTime > 0.1) deltaTime = 0.1;

                    handleInput(deltaTime);

                    keysJustPressed.clear();

                    controller.update();

                    render();

                    updateUI();

                    checkAutoRestart();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
    
    private void checkAutoRestart() {
        if (isNetworkMode) {
            return;
        }
        
        if (controller.isGameOver()) {
            if (gameOverTime == 0) {
                gameOverTime = System.currentTimeMillis();
            } else {
                long elapsed = System.currentTimeMillis() - gameOverTime;
                if (elapsed >= RESTART_DELAY_MS) {
                    controller.restart();
                    this.gameRenderer = new MinerRaceRenderer();
                    gameOverTime = 0;
                    uiManager.setStatusText("Игра автоматически перезапущена!");
                }
            }
        } else {
            if (gameOverTime != 0) {
                gameOverTime = 0;
            }
        }
    }
    
    private void handleInput(double deltaTime) {

        if (!controller.isRunning()) return;
        inputHandler.handleInput(pressedKeys, keysJustPressed, deltaTime);

    }
    
    private void setupControls() {
        this.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();

            if (isNetworkMode) {
                if (code == KeyCode.P) {
                    int pid = networkController.getPlayerId();
                    if (pid == 1) {
                         System.out.println("Host pressed P. Ready: " + controller.isReadyToStart() + ", Running: " + controller.isRunning());
                         if (controller.isReadyToStart() && !controller.isRunning()) {
                             System.out.println("Sending start command...");
                             networkController.sendStartGame();
                         }
                    }
                    return;
                }
                if (code == KeyCode.R) return;
            } else {
                if (code == KeyCode.P) {
                    if (!controller.isRunning()) {
                        controller.start();
                        if (gameLoop != null) gameLoop.start();
                        uiManager.setStatusText("Игра идет! Управление: Игрок 1 (WASD), Игрок 2 (Стрелки)");
                        this.requestFocus();
                    } else if (controller.isGameOver()) {
                        controller.restart();
                        gameRenderer = new MinerRaceRenderer();
                        gameOverTime = 0;
                        uiManager.setStatusText("Игра началась заново!");
                        this.requestFocus();
                    }
                    return;
                }
                
                if (code == KeyCode.R) {
                    controller.restart();
                    gameRenderer = new MinerRaceRenderer();
                    gameOverTime = 0;
                    uiManager.setStatusText("Игра перезапущена!");
                    return;
                }
            }
            
            if (!pressedKeys.contains(code)) {
                keysJustPressed.add(code);
            }
            pressedKeys.add(code);
        });
        
        this.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            keysJustPressed.remove(event.getCode());
        });
        
        this.setFocusTraversable(true);
        this.setOnMouseClicked(e -> this.requestFocus());
        this.requestFocus();
    }
    
    private void render() {
        GameState gameState = controller.getGameState();

        gameRenderer.render(gc, gameState, canvasWidth, canvasHeight);

        hudRenderer.render(gc, gameState, canvasWidth, canvasHeight);
    }
    
    private void updateUI() {
        GameState gameState = controller.getGameState();
        boolean isConnected = false;
        int pid = -1;
        
        if (networkController != null) {
            isConnected = networkController.isConnected();
            pid = networkController.getPlayerId();
        }
        
        uiManager.updateUI(gameState, controller.getFormattedGameTime(), isNetworkMode, pid, isConnected);
    }
    
    public void start() {
        if (!controller.isRunning()) {
            controller.start();
        }
        if (gameLoop != null) {
            gameLoop.start();
        }

        this.setFocusTraversable(true);
        this.requestFocus();
        
        uiManager.setStatusText("Ожидание...");
        javafx.application.Platform.runLater(this::requestFocus);
    }
    
    public void stop() {
        gameLoop.stop();
        controller.stop();
    }
}
