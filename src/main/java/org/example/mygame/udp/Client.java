package org.example.mygame.udp;

import org.example.mygame.fx.model.GameState;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Client implements IClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    
    private Thread listenerThread;
    private Thread connectionThread;
    
    private volatile boolean connected = false;
    private int playerId = -1;
    private Consumer<GameState> stateUpdateCallback;
    private Consumer<String> messageCallback;
    private volatile boolean running = false;
    
    public Client() {
    }
    
    @Override
    public void connect(String ip, int port) {
        try {
            this.serverAddress = InetAddress.getByName(ip);
            this.serverPort = port;
            this.socket = new DatagramSocket();
            this.running = true;

            listenerThread = new Thread(this::listen);
            listenerThread.setDaemon(true);
            listenerThread.start();

            connectionThread = new Thread(this::connectionLoop);
            connectionThread.setDaemon(true);
            connectionThread.start();
            
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void connectionLoop() {
        while (running && !connected) {
            sendCommand(NetworkConstants.CMD_CONNECT);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private void listen() {
        byte[] buffer = new byte[NetworkConstants.BUFFER_SIZE];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                Object data = deserialize(packet.getData(), 0, packet.getLength());
                
                if (data instanceof GameState) {
                    if (stateUpdateCallback != null) {
                        stateUpdateCallback.accept((GameState) data);
                    }
                } else if (data instanceof String) {
                    handleMessage((String) data);
                }
                
            } catch (SocketException e) {
                if (running) System.out.println("Socket closed");
            } catch (Exception e) {
                if (running) System.err.println("Read error: " + e.getMessage());
            }
        }
    }
    
    private void handleMessage(String message) {
        if (message.startsWith(NetworkConstants.PREFIX_PLAYER_ID)) {
            playerId = Integer.parseInt(message.substring(NetworkConstants.PREFIX_PLAYER_ID.length()));
            connected = true;
                    System.out.println("Assigned Player ID: " + playerId);
                    if (messageCallback != null) {
                        messageCallback.accept("You are Player " + playerId);
                    }
        } else if (message.equals(NetworkConstants.MSG_GAME_START)) {
                    System.out.println("Game started signal received");
                    if (messageCallback != null) {
                messageCallback.accept(NetworkConstants.MSG_GAME_START);
                    }
        } else if (message.equals(NetworkConstants.MSG_READY_TO_START)) {
                     System.out.println("Server ready to start");
                if (messageCallback != null) {
                 messageCallback.accept(NetworkConstants.MSG_READY_TO_START);
            }
        }
    }
    
    public void sendInput(String command, double deltaTime) {
        if (connected) {
             String msg = NetworkConstants.PREFIX_INPUT + command + ":" + deltaTime;
             sendObject(msg);
        }
    }
    
    public void sendCommand(String command) {
        sendObject(command);
    }
    
    private void sendObject(Object object) {
        if (socket != null && !socket.isClosed()) {
            try {
                byte[] data = serialize(object);
                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
                socket.send(packet);
            } catch (IOException e) {
                System.err.println("Error sending: " + e.getMessage());
            }
        }
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
    
    public void setStateUpdateCallback(Consumer<GameState> callback) {
        this.stateUpdateCallback = callback;
    }
    
    public void setMessageCallback(Consumer<String> callback) {
        this.messageCallback = callback;
    }
    
    public void disconnect() {
        stop();
    }
    
    public void stop() {
        running = false;
        connected = false;
        if (connectionThread != null) connectionThread.interrupt();
        if (listenerThread != null) listenerThread.interrupt();
            if (socket != null && !socket.isClosed()) socket.close();
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getPlayerId() {
        return playerId;
    }
}
