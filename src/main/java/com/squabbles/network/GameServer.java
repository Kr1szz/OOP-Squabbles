package com.squabbles.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer implements Runnable {
    private int port;
    private ServerSocket serverSocket;
    private boolean running = false;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private LobbyServer lobby;

    public GameServer(int port) {
        this.port = port;
        this.lobby = new LobbyServer();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server started on port " + port);
            System.out.println("Lobby System Active. Waiting for players...");

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection received.");

                // Create a new client handler
                // We assign a temporary ID, or let the Lobby handle it.
                // For simplicity, let's generate a random ID or increment.
                int playerId = (int) (Math.random() * 10000);
                ServerClient client = new ServerClient(socket, playerId, lobby);

                lobby.addClient(client);
                pool.execute(client);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
            pool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    // Main method for testing server independently
    public static void main(String[] args) {
        new Thread(new GameServer(NetworkProtocol.PORT)).start();
    }
}
