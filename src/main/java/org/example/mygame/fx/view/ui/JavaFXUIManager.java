package org.example.mygame.fx.view.ui;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.model.Miner;

public class JavaFXUIManager implements IUIManager {
    private final Text statusText;
    private final Text miner1Info;
    private final Text miner2Info;

    public JavaFXUIManager(VBox container) {
        statusText = new Text("Нажмите P для начала игры (в локальном режиме) | Цель: достичь глубины 90 блоков!");
        statusText.setFont(Font.font(14));
        statusText.setWrappingWidth(800);
        
        miner1Info = new Text();
        miner1Info.setFont(Font.font(14));
        miner1Info.setFill(Color.BLUE);
        
        miner2Info = new Text();
        miner2Info.setFont(Font.font(14));
        miner2Info.setFill(Color.RED);
        
        container.getChildren().addAll(statusText, miner1Info, miner2Info);
    }

    @Override
    public void updateUI(GameState gameState, String formattedTime, boolean isNetworkMode, int playerId, boolean isConnected) {
        if (gameState == null) return;

        if (isNetworkMode) {
            String controls = "";
            if (playerId == 1) {
                controls = "Вы - Шахтер 1 (СИНИЙ). Управление: WASD";
            } else if (playerId == 2) {
                controls = "Вы - Шахтер 2 (КРАСНЫЙ). Управление: WASD или Стрелки";
            } else {
                controls = "Наблюдатель";
            }
            
            if (!isConnected) {
                statusText.setText("Отключен от сервера. " + controls);
            } else if (gameState.isGameOver()) {
                if (gameState.getWinner() != null) {
                    String winnerName = gameState.getWinner().getName();
                    String winnerText = "ПОБЕДИТЕЛЬ: " + winnerName + "!";
                    statusText.setText(winnerText);
                    statusText.setFill(winnerName.contains("1") ? Color.BLUE : Color.RED);
                } else {
                    statusText.setText("Игра окончена! Ожидание результата... " + controls);
                }
            } else {
                if (gameState.getMiners().size() >= 2 && !gameState.isGameOver() && formattedTime.equals("00:00")) {
                     if (playerId == 1) {
                         statusText.setText("Оба игрока готовы. Нажмите P для старта! " + controls);
                     } else {
                         statusText.setText("Ожидание запуска игры хостом... " + controls);
                     }
                } else {
                    statusText.setText("Сетевая игра. " + controls);
                }
            }
        } else {
            if (gameState.isGameOver()) {
                if (gameState.getWinner() != null) {
                    String winnerName = gameState.getWinner().getName();
                    statusText.setText("ПОБЕДИТЕЛЬ: " + winnerName + "! Нажмите P для новой игры или R для перезапуска");
                    statusText.setFill(winnerName.contains("1") ? Color.BLUE : Color.RED);
                } else {
                    statusText.setText("Игра окончена! Нажмите P для новой игры или R для перезапуска");
                }
            } else {
                statusText.setText("Локальная игра. Игрок 1: WASD | Игрок 2: Стрелки");
                statusText.setFill(Color.BLACK);
            }
        }
        
        if (gameState.getMiners().size() >= 2) {
            Miner miner1 = gameState.getMiners().get(0);
            Miner miner2 = gameState.getMiners().get(1);
            
            String info1 = String.format("%s: Глубина %.1f/%.0f | Блоков: %d | Скорость: %.2f", 
                miner1.getName(), miner1.getDepth(), GameState.BOTTOM_DEPTH, miner1.getBlocksMined(), 
                miner1.getCurrentMiningSpeed());
            if (miner1.getActiveOreEffect() != null) {
                info1 += " | Эффект: " + miner1.getActiveOreEffect().name + 
                    " (" + String.format("%.1f", miner1.getOreEffectTime()) + "с)";
            }
            
            String info2 = String.format("%s: Глубина %.1f/%.0f | Блоков: %d | Скорость: %.2f", 
                miner2.getName(), miner2.getDepth(), GameState.BOTTOM_DEPTH, miner2.getBlocksMined(), 
                miner2.getCurrentMiningSpeed());
            if (miner2.getActiveOreEffect() != null) {
                info2 += " | Эффект: " + miner2.getActiveOreEffect().name + 
                    " (" + String.format("%.1f", miner2.getOreEffectTime()) + "с)";
            }
            
            miner1Info.setText(info1);
            miner2Info.setText(info2);
        }
    }

    @Override
    public void setStatusText(String text) {
        statusText.setText(text);
    }

    @Override
    public void setMinerInfo(int minerIndex, String text) {
        if (minerIndex == 0) miner1Info.setText(text);
        else if (minerIndex == 1) miner2Info.setText(text);
    }

    @Override
    public void clear() {
    }
}
