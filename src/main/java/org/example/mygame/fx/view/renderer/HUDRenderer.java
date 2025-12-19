package org.example.mygame.fx.view.renderer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.mygame.fx.controller.IGameController;
import org.example.mygame.fx.controller.NetworkGameController;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.model.Miner;

public class HUDRenderer implements IRenderer {
    private final IGameController controller;
    private final boolean isNetworkMode;

    private long gameOverTime = 0;
    private static final long RESTART_DELAY_MS = 3000;

    public HUDRenderer(IGameController controller) {
        this.controller = controller;
        this.isNetworkMode = controller instanceof NetworkGameController;
    }

    @Override
    public void render(GraphicsContext gc, GameState gameState, double width, double height) {
        if (gameState == null) return;
        
        double maxDepth = GameState.BOTTOM_DEPTH - 1;
        double halfWidth = width / 2.0;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(18));
        String timeText = "Время: " + controller.getFormattedGameTime();
        double timeWidth = timeText.length() * 9;
        gc.fillText(timeText, (width - timeWidth) / 2, 24);

        if (isNetworkMode) {
            gc.setFill(Color.CYAN);
            gc.setFont(Font.font(12));
            int currentId = ((NetworkGameController) controller).getPlayerId();
            String networkInfo = "Сетевой режим | Игрок " + currentId;
            gc.fillText(networkInfo, 10, height - 20);
            
            if (!((NetworkGameController) controller).isConnected()) {
                gc.setFill(Color.RED);
                gc.fillText("ОТКЛЮЧЕН ОТ СЕРВЕРА", 10, height - 5);
            }
        }

        if (gameState.getMiners().size() >= 1) {
            Miner m1 = gameState.getMiners().get(0);
            gc.setFill(Color.BLUE);
            gc.setFont(Font.font(14));
            String info1 = String.format("%s | Глубина: %.1f/%.0f | Блоков: %d",
                m1.getName(), m1.getDepth(), maxDepth, m1.getBlocksMined());
            gc.fillText(info1, 10, 20);
        }
        if (gameState.getMiners().size() >= 2) {
            Miner m2 = gameState.getMiners().get(1);
            gc.setFill(Color.RED);
            gc.setFont(Font.font(14));
            String info2 = String.format("%s | Глубина: %.1f/%.0f | Блоков: %d",
                m2.getName(), m2.getDepth(), maxDepth, m2.getBlocksMined());
            double infoWidth = info2.length() * 8;
            gc.fillText(info2, width - infoWidth - 10, 20);
        }

        double barWidth = halfWidth - 30;
        double barHeight = 8;
        
        for (int i = 0; i < Math.min(2, gameState.getMiners().size()); i++) {
            Miner miner = gameState.getMiners().get(i);
            double progress = maxDepth > 0 ? Math.min(miner.getDepth() / maxDepth, 1.0) : 0.0;
            double x = i == 0 ? 10 : halfWidth + 10;
            double y = 28;
            
            gc.setFill(Color.GRAY);
            gc.fillRect(x, y, barWidth, barHeight);
            gc.setFill(i == 0 ? Color.BLUE : Color.RED);
            gc.fillRect(x, y, barWidth * progress, barHeight);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, barWidth, barHeight);
        }

        if (gameState.isGameOver()) {
            if (gameState.getWinner() != null) {
                renderWinnerScreen(gc, gameState, width, height);
            } else {
                gc.setFill(new Color(0, 0, 0, 0.7));
                gc.fillRect(0, 0, width, height);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(24));
                String waitingText = "Игра окончена. Ожидание результата...";
                double textWidth = waitingText.length() * 12;
                gc.fillText(waitingText, (width - textWidth) / 2, height / 2);
            }
        } else {
            gameOverTime = 0;
        }
    }
    
    private void renderWinnerScreen(GraphicsContext gc, GameState gameState, double width, double height) {
        if (gameOverTime == 0) {
            gameOverTime = System.currentTimeMillis();
        }
        
        gc.setFill(new Color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);
        
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font(36));
        String winnerText = "ПОБЕДИТЕЛЬ: " + gameState.getWinner().getName();
        double textWidth = winnerText.length() * 20;
        gc.fillText(winnerText, (width - textWidth) / 2, height / 2 - 20);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(20));
        
        if (gameOverTime > 0) {
            long elapsed = System.currentTimeMillis() - gameOverTime;
            long remaining = RESTART_DELAY_MS - elapsed;
            if (remaining < 0) remaining = 0;
            
            int seconds = (int) Math.ceil(remaining / 1000.0);
            String restartText = "Автоматический перезапуск через " + seconds + " сек...";
            double restartWidth = restartText.length() * 10;
            gc.fillText(restartText, (width - restartWidth) / 2, height / 2 + 30);
            
            gc.setFont(Font.font(16));
            String manualText = "Или нажмите P для немедленного перезапуска";
            double manualWidth = manualText.length() * 8;
            gc.fillText(manualText, (width - manualWidth) / 2, height / 2 + 60);
        } else {
            String restartText = "Нажмите P для новой игры или R для перезапуска";
            double restartWidth = restartText.length() * 10;
            gc.fillText(restartText, (width - restartWidth) / 2, height / 2 + 30);
        }
    }

    @Override
    public void cleanup() {
    }

    public long getGameOverTime() {
        return gameOverTime;
    }
}
