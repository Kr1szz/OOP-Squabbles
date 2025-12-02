package com.squabbles.view;

import com.squabbles.Main;
import com.squabbles.network.GameClient;
import com.squabbles.network.GameServer;
import com.squabbles.network.NetworkProtocol;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;

public class SessionSetupView {
    private Label statusLabel;
    private GameClient client;
    private GameServer server; // Keep reference to stop if needed, or just let it run

    public Scene getScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label titleLabel = new Label("Session Setup");
        titleLabel.getStyleClass().add("header-label");

        // Host Section
        VBox hostBox = new VBox(10);
        hostBox.setAlignment(Pos.CENTER);
        Button hostButton = new Button("Generate Unique Game ID (Host)");
        Label gameIdLabel = new Label();
        gameIdLabel.getStyleClass().add("status-label");

        Button copyHostInfoButton = new Button("Copy Host Details");
        copyHostInfoButton.getStyleClass().add("secondary-button");
        copyHostInfoButton.setDisable(true);

        hostBox.getChildren().addAll(hostButton, gameIdLabel, copyHostInfoButton);

        // Join Section
        VBox joinBox = new VBox(10);
        joinBox.setAlignment(Pos.CENTER);

        TextField ipField = new TextField("localhost");
        ipField.setPromptText("Enter Host IP");
        ipField.setMaxWidth(200);

        TextField gameIdField = new TextField();
        gameIdField.setPromptText("Enter Game ID (Port)");
        gameIdField.setMaxWidth(200);

        Button joinButton = new Button("Join Game");
        joinBox.getChildren().addAll(ipField, gameIdField, joinButton);

        statusLabel = new Label("Select an option...");
        statusLabel.getStyleClass().add("status-label");

        Button backButton = new Button("Back to Main Menu");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> Main.setScene(new WelcomeView().getScene()));

        // Logic
        client = new GameClient();

        hostButton.setOnAction(e -> {
            // Start Server on a random high port to avoid "Address already in use"
            int port = generateRandomPort();
            server = new GameServer(port, 2);
            new Thread(server).start();

            // Try to determine a useful local IP address to show to other players
            String ipText = "unknown";
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                ipText = hostAddress;
            } catch (Exception ex) {
                // If we can't resolve, fall back to localhost hint
                ipText = "localhost";
            }

            String hostInfo = "Host IP: " + ipText + " | Game ID: " + port;
            gameIdLabel.setText(hostInfo);
            statusLabel.setText("Waiting for Player 2...");
            hostButton.setDisable(true);
            joinButton.setDisable(true); // Can't join if hosting in this simple UI
            copyHostInfoButton.setDisable(false);

            // Connect as Player 1
            connectToServer("localhost", port);
        });

        copyHostInfoButton.setOnAction(e -> {
            String text = gameIdLabel.getText();
            if (text != null && !text.isEmpty()) {
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);
                statusLabel.setText("Host details copied to clipboard.");
            }
        });

        joinButton.setOnAction(e -> {
            String ip = ipField.getText();
            String idStr = gameIdField.getText();

            if (ip.isEmpty()) {
                statusLabel.setText("Please enter Host IP.");
                return;
            }
            if (idStr.isEmpty()) {
                statusLabel.setText("Please enter a Game ID.");
                return;
            }

            try {
                int port = Integer.parseInt(idStr);
                statusLabel.setText("Connecting to " + ip + "...");
                connectToServer(ip, port);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid Game ID.");
            }
        });

        root.getChildren().addAll(titleLabel, hostBox, new Label("- OR -"), joinBox, statusLabel, backButton);

        return new Scene(root, 800, 600);
    }

    private void connectToServer(String host, int port) {
        try {
            client.connect(host, port, this::handleMessage);
        } catch (IOException e) {
            statusLabel.setText("Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a random high port (50000-59999) that appears to be free.
     * Falls back to the default NetworkProtocol.PORT if all attempts fail.
     */
    private int generateRandomPort() {
        for (int i = 0; i < 20; i++) {
            int candidate = 50000 + (int) (Math.random() * 10000);
            try (ServerSocket ignored = new ServerSocket(candidate)) {
                return candidate;
            } catch (IOException ignoredEx) {
                // Try another candidate
            }
        }
        return NetworkProtocol.PORT;
    }

    private void handleMessage(String message) {
        if (message.startsWith(NetworkProtocol.MSG_WELCOME)) {
            String playerId = message.split(" ")[1];
            Platform.runLater(
                    () -> statusLabel.setText("Connected as Player " + playerId + ". Waiting for game start..."));
        } else if (message.startsWith(NetworkProtocol.MSG_PLAYER_JOINED)) {
            String count = message.split(" ")[1];
            Platform.runLater(() -> statusLabel.setText("Player joined! " + count));
        } else if (message.startsWith(NetworkProtocol.MSG_START_GAME)) {
            Platform.runLater(() -> {
                // Transition to GameView (true => multiplayer mode)
                Main.setScene(new GameView(client, true).getScene());
            });
        }
    }
}
