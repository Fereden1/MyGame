package org.example.mygame.fx.view.ui;

import org.example.mygame.fx.model.GameState;

public interface IUIManager {
    void updateUI(GameState gameState, String formattedTime, boolean isNetworkMode, int playerId, boolean isConnected);
    void setStatusText(String text);
    void setMinerInfo(int minerIndex, String text);
    void clear();
}
