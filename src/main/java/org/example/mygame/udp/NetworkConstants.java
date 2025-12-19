package org.example.mygame.udp;

public class NetworkConstants {

    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_PORT = 5555;
    public static final int BUFFER_SIZE = 65507;

    public static final String CMD_CONNECT = "CONNECT";
    public static final String CMD_START = "CMD:START";
    
    public static final String PREFIX_INPUT = "INPUT:";
    public static final String PREFIX_STATE = "STATE:";
    public static final String PREFIX_PLAYER_ID = "PLAYER_ID:";
    public static final String MSG_GAME_START = "GAME_START";
    public static final String MSG_READY_TO_START = "READY_TO_START";

    public static final String INPUT_TURN_LEFT = "TURN_LEFT";
    public static final String INPUT_TURN_RIGHT = "TURN_RIGHT";
    public static final String INPUT_TURN_DOWN = "TURN_DOWN";
    public static final String INPUT_MINE = "MINE";
    public static final String INPUT_MOVE = "MOVE";
}
