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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

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
        hostBox.getChildren().addAll(hostButton, gameIdLabel);

        // Join Section
        VBox joinBox = new VBox(10);
        joinBox.setAlignment(Pos.CENTER);
        TextField gameIdField = new TextField();
        gameIdField.setPromptText("Enter Game ID (Port)");
        gameIdField.setMaxWidth(200);
        Button joinButton = new Button("Join Game");
        joinBox.getChildren().addAll(gameIdField, joinButton);

        statusLabel = new Label("Select an option...");

        // Logic
        client = new GameClient();

        hostButton.setOnAction(e -> {
            // Start Server
            int port = NetworkProtocol.PORT; // Or random
            server = new GameServer(port, 2);
            new Thread(server).start();

            gameIdLabel.setText("Game ID: " + port);
            statusLabel.setText("Waiting for Player 2...");
            hostButton.setDisable(true);
            joinButton.setDisable(true); // Can't join if hosting in this simple UI

            // Connect as Player 1
            connectToServer("localhost", port);
        });

        joinButton.setOnAction(e ->

        {
            String idStr = gameIdField.getText();
            if (idStr.isEmpty()) {
                statusLabel.setText("Please enter a Game ID.");
                return;
            }
            try {
                int port = Integer.parseInt(idStr);
                statusLabel.setText("Connecting...");
                connectToServer("localhost", port);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid Game ID.");
            }
        });

        root.getChildren().addAll(titleLabel, hostBox, new Label("- OR -"), joinBox, statusLabel);

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
                // Transition to GameView
                Main.setScene(new GameView(client).getScene());
            });
        }
    }
}
