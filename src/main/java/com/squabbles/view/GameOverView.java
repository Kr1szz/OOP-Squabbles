package com.squabbles.view;

import com.squabbles.Main;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameOverView {
    private String winnerText;
    private String playerName;

    public GameOverView(String winnerText, String playerName) {
        this.playerName = playerName;
        if (winnerText != null && winnerText.startsWith("GAME_OVER")) {
            String[] parts = winnerText.split(" ", 2);
            this.winnerText = parts.length > 1 ? parts[1].trim() : "Unknown";
        } else {
            this.winnerText = winnerText;
        }
    }

    public Scene getScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label header = new Label("Game Over!");
        header.getStyleClass().add("header-label");

        Label winnerLabel = new Label(winnerText);
        winnerLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #ffd700; -fx-font-weight: bold;");

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setOnAction(e -> Main.setScene(new SessionSetupView(playerName).getScene()));

        Button exitButton = new Button("Main Menu");
        exitButton.setOnAction(e -> Main.setScene(new WelcomeView().getScene()));
        exitButton.setStyle("-fx-background-color: #d9534f;"); // Red for exit

        root.getChildren().addAll(header, winnerLabel, playAgainButton, exitButton);

        return new Scene(root, 800, 600);
    }
}
