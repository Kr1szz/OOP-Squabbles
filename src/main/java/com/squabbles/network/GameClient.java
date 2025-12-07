package com.squabbles.network;

import javafx.application.Platform;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private Consumer<String> onMessageReceived;
    private int playerId = -1;

    public int getPlayerId() {
        return playerId;
    }

    public void connect(String host, int port, Consumer<String> onMessageReceived) throws IOException {
        this.onMessageReceived = onMessageReceived;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new Scanner(socket.getInputStream());

        new Thread(() -> {
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith(NetworkProtocol.MSG_WELCOME)) {
                    try {
                        this.playerId = Integer.parseInt(line.split(" ")[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Platform.runLater(() -> {
                    if (this.onMessageReceived != null) {
                        this.onMessageReceived.accept(line);
                    }
                });
            }
        }).start();
    }

    public void sendMatchAttempt(int iconId) {
        if (out != null) {
            out.println(NetworkProtocol.MSG_MATCH_ATTEMPT + " " + iconId);
        }
    }

    public void sendRequestState() {
        if (out != null) {
            out.println(NetworkProtocol.MSG_REQUEST_STATE);
        }
    }

    public void sendEndGame() {
        if (out != null) {
            out.println(NetworkProtocol.MSG_END_GAME);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void disconnect() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.onMessageReceived = handler;
    }
}
