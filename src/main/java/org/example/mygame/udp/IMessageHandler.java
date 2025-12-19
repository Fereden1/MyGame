package org.example.mygame.udp;

public interface IMessageHandler {

    void handleMessage(int playerId, String message);
    void handleInput(int playerId, String command, double deltaTime);

}

