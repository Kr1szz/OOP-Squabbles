package com.squabbles.view;

import com.squabbles.Main;
import com.squabbles.network.GameClient;
import com.squabbles.network.GameServer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class WelcomeView {
    private javafx.scene.control.TextField nameField;

    public Scene getScene() {
        com.squabbles.util.DatabaseManager.initialize();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label titleLabel = new Label("OOP Squabbles (Icon Matcher Game)");
        titleLabel.getStyleClass().add("header-label");

        nameField = new javafx.scene.control.TextField();
        nameField.setPromptText("Enter your name");
        nameField.setMaxWidth(200);

        Button submitNameButton = new Button("Submit Name");
        Label welcomeLabel = new Label();
        welcomeLabel.getStyleClass().add("message-label");

        Button singlePlayerButton = new Button("Single Player (vs Bot)");
        singlePlayerButton.setOnAction(e -> startSinglePlayer(true));
        singlePlayerButton.setDisable(true);

        Button practiceButton = new Button("Practice Mode (No AI)");
        practiceButton.setOnAction(e -> startSinglePlayer(false));
        practiceButton.setDisable(true);

        Button multiplayerButton = new Button("Multiplayer");
        multiplayerButton.setOnAction(e -> Main.setScene(new SessionSetupView(nameField.getText().trim()).getScene()));
        multiplayerButton.setDisable(true);

        submitNameButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                com.squabbles.util.DatabaseManager.addPlayer(name);
                welcomeLabel.setText("Welcome, " + name + "!");
                singlePlayerButton.setDisable(false);
                practiceButton.setDisable(false);
                multiplayerButton.setDisable(false);
                nameField.setDisable(true);
                submitNameButton.setDisable(true);
            }
        });

        root.getChildren().addAll(titleLabel, nameField, submitNameButton, welcomeLabel, singlePlayerButton, practiceButton, multiplayerButton);

        return new Scene(root, 800, 600);
    }

    private void startSinglePlayer(boolean withBot) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            name = "Player";
        }
        com.squabbles.util.DatabaseManager.addPlayer(name);
        final String playerName = name;

        // Start a local server for 1 player (plus bot)
        int randomPort = 50000 + (int) (Math.random() * 10000);

        GameServer server = new GameServer(randomPort);
        new Thread(server).start();

        // Connect client in a background thread to avoid freezing UI
        new Thread(() -> {
            GameClient client = new GameClient();
            try {
                // Retry logic for connection
                boolean connected = false;
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(200); // Wait for server start
                        client.connect("localhost", randomPort, message -> {
                            if (message.startsWith(com.squabbles.network.NetworkProtocol.MSG_WELCOME)) {
                                // Request Bot Game if needed
                                if (withBot) {
                                    client.sendMessage(com.squabbles.network.NetworkProtocol.MSG_PLAY_BOT + " 1 " + playerName); // Difficulty 1
                                } else {
                                    client.sendMessage(com.squabbles.network.NetworkProtocol.MSG_PLAY_BOT + " 0 " + playerName); // 0 = Practice/No Bot
                                }
                            } else if (message.startsWith(com.squabbles.network.NetworkProtocol.MSG_START_GAME)) {
                                javafx.application.Platform.runLater(() -> Main.setScene(new GameView(client, false, playerName).getScene()));
                            } else if (message.startsWith(com.squabbles.network.NetworkProtocol.MSG_TURN_UPDATE)) {
                                // Ensure turn updates are handled
                            }
                        });
                        connected = true;
                        break;
                    } catch (Exception e) {
                        System.out.println("Connection attempt " + (i + 1) + " failed. Retrying...");
                    }
                }
                
                if (!connected) {
                    javafx.application.Platform.runLater(() -> {
                        System.err.println("Failed to connect to local server.");
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
