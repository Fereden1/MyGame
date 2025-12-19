package org.example.mygame.udp;

import org.example.mygame.fx.model.GameState;
import java.util.function.Consumer;

public interface IClient {

    void connect(String host, int port);
    void disconnect();
    boolean isConnected();
    void sendInput(String command, double deltaTime);
    void setStateUpdateCallback(Consumer<GameState> callback);
    void setMessageCallback(Consumer<String> callback);
    int getPlayerId();

}

