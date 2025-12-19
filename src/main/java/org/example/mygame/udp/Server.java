package org.example.mygame.udp;

import org.example.mygame.fx.controller.GameController;
import org.example.mygame.fx.model.GameState;
import org.example.mygame.fx.model.Miner;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Server implements IServer, IMessageHandler {
    private DatagramSocket socket;
    private final Map<SocketAddress, Integer> clients = new ConcurrentHashMap<>();
    private GameController gameController;
    private volatile boolean gameStarted = false;
    private Thread gameLoopThread;
    private Thread receiveThread;
    private final int port;
    private final String bindAddress;
    private volatile boolean running = false;
    private long gameOverTime = 0;
    private static final long RESTART_DELAY_MS = 5000;
    
    public Server(int port) {
        this(port, NetworkConstants.DEFAULT_BIND_ADDRESS);
    }

    public Server(int port, String bindAddress) {
        this.port = port;
        this.bindAddress = bindAddress;
        this.gameController = new GameController();
    }
    
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : NetworkConstants.DEFAULT_PORT;
        Server server = new Server(port);
        server.start();
    }
    
    public void start() {
        try {
            running = true;
            if (bindAddress != null && !bindAddress.equals("0.0.0.0") && !bindAddress.isEmpty()) {
                socket = new DatagramSocket(new InetSocketAddress(InetAddress.getByName(bindAddress), port));
                System.out.println("UDP Server listening on " + bindAddress + ":" + port);
            } else {
                socket = new DatagramSocket(port);
                System.out.println("UDP Server listening on all interfaces (0.0.0.0):" + port);
            }
            
            System.out.println("Waiting for players...");
            
            receiveThread = new Thread(this::receiveLoop);
            receiveThread.setDaemon(true);
            receiveThread.start();
            
        } catch (IOException e) {
            if (running) {
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void receiveLoop() {
        byte[] buffer = new byte[NetworkConstants.BUFFER_SIZE];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                Object data = deserialize(packet.getData(), 0, packet.getLength());
                SocketAddress sender = packet.getSocketAddress();
                
                if (data instanceof String) {
                    handleStringMessage(sender, (String) data);
                }
                
            } catch (IOException e) {
                if (running) System.err.println("Receive error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("Deserialization error: " + e.getMessage());
            }
        }
    }
    
    private void handleStringMessage(SocketAddress sender, String message) {
        if (message.equals(NetworkConstants.CMD_CONNECT)) {
            handleConnect(sender);
        } else {
            Integer playerId = clients.get(sender);
            if (playerId != null) {
                handleMessage(playerId, message);
            }
        }
    }
    
    private void handleConnect(SocketAddress sender) {
        if (!clients.containsKey(sender)) {
            if (clients.size() >= 2) {
                System.out.println("Connection rejected: Server full");
                return;
            }
            
            int newPlayerId = clients.size() + 1;
            clients.put(sender, newPlayerId);
            System.out.println("Player " + newPlayerId + " connected from " + sender);

            sendTo(sender, NetworkConstants.PREFIX_PLAYER_ID + newPlayerId);
            
            if (clients.size() == 2) {
                System.out.println("Both players connected! Waiting for host to start...");
                broadcastMessage(NetworkConstants.MSG_READY_TO_START);
            }
        } else {
             int id = clients.get(sender);
             sendTo(sender, NetworkConstants.PREFIX_PLAYER_ID + id);
        }
    }
    
    private void restartGame() {
        System.out.println("Restarting game...");
        gameController.restart();
        gameOverTime = 0;
        broadcastMessage(NetworkConstants.MSG_GAME_START);
    }
    
    private void startGame() {
        if (gameStarted) return;
        
        System.out.println("Game starting...");
        gameStarted = true;
        gameController.start();
        gameOverTime = 0;

        broadcastGameState();
        broadcastMessage(NetworkConstants.MSG_GAME_START);
        
        gameLoopThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            final double TARGET_FPS = 30.0;
            final double OPTIMAL_TIME = 1_000_000_000.0 / TARGET_FPS;

            while (running && gameStarted) {
                long currentTime = System.nanoTime();
                double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
                lastTime = currentTime;
                
                if (deltaTime > 0.1) deltaTime = 0.1;
                
                synchronized (gameController) {
                    if (!gameController.isGameOver()) {
                        gameController.update();
                    } else {
                        if (gameOverTime == 0) {
                            gameOverTime = System.currentTimeMillis();
                            System.out.println("Game over! Winner: " + 
                                (gameController.getGameState().getWinner() != null 
                                    ? gameController.getGameState().getWinner().getName() 
                                    : "unknown"));
                        } else {
                            long elapsed = System.currentTimeMillis() - gameOverTime;
                            if (elapsed >= RESTART_DELAY_MS) {
                                System.out.println("Auto-restarting game...");
                                restartGame();
                                gameOverTime = 0;
                            }
                        }
                    }
                }
                
                broadcastGameState();
                
                long sleepTime = (long) ((lastTime - System.nanoTime() + OPTIMAL_TIME) / 1_000_000);
                
                try {
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    }
    
    @Override
    public void handleMessage(int playerId, String message) {
        if (message.equals(NetworkConstants.CMD_START)) {
            System.out.println("Received start command from Player " + playerId);
            if (playerId == 1 && clients.size() >= 2) { 
                startGame();
            }
            return;
        }
        
        if (message.startsWith(NetworkConstants.PREFIX_INPUT)) {
            String[] parts = message.substring(NetworkConstants.PREFIX_INPUT.length()).split(":");
            if (parts.length >= 1) {
                String command = parts[0];
                double deltaTime = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.016;
                handleInput(playerId, command, deltaTime);
            }
        }
    }
    
    @Override
    public synchronized void handleInput(int playerId, String command, double deltaTime) {
        if (!gameStarted || gameController.isGameOver()) return;
        
        GameState gameState = gameController.getGameState();
        int minerId = playerId;
        
        synchronized (gameController) {
            if (deltaTime <= 0) deltaTime = 0.016;
            if (deltaTime > 0.1) deltaTime = 0.1;

            System.out.println("Processing input from Player " + playerId + ": " + command);

            switch (command) {
                case NetworkConstants.INPUT_TURN_LEFT:
                    gameState.turnMiner(minerId, Miner.Direction.LEFT);
                    break;
                case NetworkConstants.INPUT_TURN_RIGHT:
                    gameState.turnMiner(minerId, Miner.Direction.RIGHT);
                    break;
                case NetworkConstants.INPUT_TURN_DOWN:
                    gameState.turnMiner(minerId, Miner.Direction.DOWN);
                    break;
                case NetworkConstants.INPUT_MINE:
                    gameState.mineBlock(minerId, deltaTime);
                    break;
                case NetworkConstants.INPUT_MOVE:
                    gameState.moveMinerInDirection(minerId, deltaTime);
                    break;
            }
        }
    }
    
    private void broadcastGameState() {
        if (clients.isEmpty()) return;
        
        GameState gameState = gameController.getGameState();
        if (gameState == null) return;
        
        synchronized (gameController) {
            broadcast(gameState);
        }
    }
    
    private void broadcastMessage(String message) {
        broadcast(message);
    }
    
    private void broadcast(Object object) {
        try {
            byte[] data = serialize(object);
            for (SocketAddress client : clients.keySet()) {
                sendPacket(client, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void sendTo(SocketAddress target, Object object) {
        try {
            byte[] data = serialize(object);
            sendPacket(target, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void sendPacket(SocketAddress target, byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, target);
        socket.send(packet);
    }
    
    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos);
             ObjectOutputStream oos = new ObjectOutputStream(gzip)) {
            oos.writeObject(object);
        }
        return baos.toByteArray();
    }
    
    private Object deserialize(byte[] data, int offset, int length) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data, offset, length);
             GZIPInputStream gzip = new GZIPInputStream(bais);
             ObjectInputStream ois = new ObjectInputStream(gzip)) {
            return ois.readObject();
        }
    }
    
    public void stop() {
        running = false;
        gameStarted = false;
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public GameController getGameController() {
        return gameController;
    }
}
