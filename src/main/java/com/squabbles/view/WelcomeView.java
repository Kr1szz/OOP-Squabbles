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
    public Scene getScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label titleLabel = new Label("OOP Squabbles (Icon Matcher Game)");
        titleLabel.getStyleClass().add("header-label");

        Button singlePlayerButton = new Button("Single Player");
        singlePlayerButton.setOnAction(e -> startSinglePlayer());

        Button multiplayerButton = new Button("Multiplayer");
        multiplayerButton.setOnAction(e -> Main.setScene(new SessionSetupView().getScene()));

        root.getChildren().addAll(titleLabel, singlePlayerButton, multiplayerButton);

        return new Scene(root, 800, 600);
    }

    private void startSinglePlayer() {
        // Start a local server for 1 player
        // Start a local server for 1 player
        // Let's pick a random port between 50000 and 60000
        int randomPort = 50000 + (int) (Math.random() * 10000);

        GameServer server = new GameServer(randomPort, 1);
        new Thread(server).start();

        // Connect client
        GameClient client = new GameClient();
        try {
            // We need to wait a bit for server to start? Usually fast enough.
            // But to be safe, we can retry or just sleep briefly.
            Thread.sleep(100);

            client.connect("localhost", randomPort, message -> {
                        if (message.startsWith(com.squabbles.network.NetworkProtocol.MSG_START_GAME)) {
                    // false => single-player mode (no 25-points message)
                    javafx.application.Platform.runLater(() -> Main.setScene(new GameView(client, false).getScene()));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
