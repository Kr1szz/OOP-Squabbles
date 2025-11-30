package com.squabbles.view;

import com.squabbles.Main;
import com.squabbles.network.GameClient;
import com.squabbles.network.NetworkProtocol;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameView {
    private GameClient client;
    private StackPane root;
    private VBox gameLayout;
    private Pane feedbackLayer;
    private HBox cardsContainer;
    private Label scoreLabel;
    private Label messageLabel;
    private Random random = new Random();

    private double lastClickX = 0;
    private double lastClickY = 0;

    // 57 Vibrant Colorful Emojis (No Black & White)
    private static final String[] EMOJIS = {
            "?", // 0 (unused)
            "🍎", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍑", "🍒", "🍍", // 1-10
            "🥝", "🥕", "🌽", "🍅", "🌶️", "🫑", "🥦", "🍏", "🥭", "🍐", // 11-20
            "🌸", "🌺", "🌻", "🌷", "🌹", "🥀", "🌼", "💐", "🪻", "💮", // 21-30
            "❤️", "💛", "💚", "💙", "💜", "🧡", "💗", "💖", "💝", "⭐", // 31-40
            "🌟", "✨", "💫", "🌈", "☀️", "🔥", "💧", "🎈", "🎀", "🎁", // 41-50
            "🏀", "⚽", "🎨", "🎭", "🍕", "🧁", "🎂" // 51-57
    };

    public GameView(GameClient client) {
        this.client = client;
    }

    private Label levelLabel;

    public Scene getScene() {
        root = new StackPane();
        root.getStyleClass().add("root");

        gameLayout = new VBox(20);
        gameLayout.setAlignment(Pos.CENTER);

        // Top Bar
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);

        javafx.scene.control.Button endButton = new javafx.scene.control.Button("End Game");
        endButton.setStyle("-fx-background-color: #ff5555; -fx-text-fill: white; -fx-font-weight: bold;");
        endButton.setOnAction(e -> client.sendEndGame());

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        levelLabel = new Label("Level: 1");
        levelLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #f1fa8c; -fx-font-weight: bold;");

        messageLabel = new Label("Find the matching icon!");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        topBar.getChildren().addAll(endButton, scoreLabel, levelLabel, messageLabel);

        // Cards Area
        cardsContainer = new HBox(50);
        cardsContainer.setAlignment(Pos.CENTER);

        gameLayout.getChildren().addAll(topBar, cardsContainer);

        // Feedback Layer (Transparent overlay)
        feedbackLayer = new Pane();
        feedbackLayer.setPickOnBounds(false); // Allow clicks to pass through

        root.getChildren().addAll(gameLayout, feedbackLayer);

        // Initialize with waiting state
        updateBoard(null, null);

        // Set listener
        client.setMessageHandler(this::handleMessage);

        // Request initial state
        client.sendRequestState();

        return new Scene(root, 1000, 700);
    }

    private void handleMessage(String message) {
        if (message.startsWith(NetworkProtocol.MSG_UPDATE_CARDS)) {
            // Format: UPDATE_CARDS CenterCard PlayerCard
            String[] parts = message.split(" ");
            if (parts.length >= 3) {
                String centerData = parts[1];
                String playerData = parts[2];
                Platform.runLater(() -> updateBoard(centerData, playerData));
            }
        } else if (message.startsWith(NetworkProtocol.MSG_MATCH_RESULT)) {
            // MSG_MATCH_RESULT success score level
            String[] parts = message.split(" ");
            boolean success = Boolean.parseBoolean(parts[1]);
            int score = Integer.parseInt(parts[2]);
            int level = (parts.length > 3) ? Integer.parseInt(parts[3]) : 1;

            Platform.runLater(() -> {
                scoreLabel.setText("Score: " + score);
                levelLabel.setText("Level: " + level);

                if (success) {
                    messageLabel.setText("Correct! +1");
                    messageLabel.setTextFill(Color.LIMEGREEN);
                    showFloatingFeedback(lastClickX, lastClickY, "+1", Color.LIMEGREEN);
                } else {
                    messageLabel.setText("Wrong! -1");
                    messageLabel.setTextFill(Color.RED);
                    showFloatingFeedback(lastClickX, lastClickY, "-1", Color.RED);
                }

                // Auto-remove feedback after 1 second
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            messageLabel.setText("Find the matching icon!");
                            messageLabel.setTextFill(Color.WHITE);
                        });
                    }
                }, 1000);
            });
        } else if (message.startsWith(NetworkProtocol.MSG_GAME_OVER)) {
            Platform.runLater(() -> Main.setScene(new GameOverView(message).getScene()));
        }
    }

    private void showFloatingFeedback(double x, double y, String text, Color color) {
        Label feedback = new Label(text);
        feedback.setTextFill(color);
        feedback.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);");

        // Adjust position to be centered on the click
        feedback.setLayoutX(x);
        feedback.setLayoutY(y);

        feedbackLayer.getChildren().add(feedback);

        // Animation: Move up and Fade out
        TranslateTransition tt = new TranslateTransition(Duration.millis(1000), feedback);
        tt.setByY(-50);

        FadeTransition ft = new FadeTransition(Duration.millis(1000), feedback);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> feedbackLayer.getChildren().remove(feedback));
        pt.play();
    }

    private void updateBoard(String centerData, String playerData) {
        cardsContainer.getChildren().clear();

        if (centerData != null && !centerData.equals("null")) {
            VBox centerBox = createCard("Center Card", 0.6, centerData, false);
            cardsContainer.getChildren().add(centerBox);
        }

        if (playerData != null && !playerData.equals("null")) {
            VBox playerBox = createCard("Your Card", 1.0, playerData, true);
            cardsContainer.getChildren().add(playerBox);
        }
    }

    private VBox createCard(String title, double scale, String cardData, boolean interactive) {
        // cardData format: ID:Icon1,Icon2...
        String[] parts = cardData.split(":");
        // String cardId = parts[0];
        String[] iconIds = parts[1].split(",");

        StackPane cardPane = new StackPane();
        // Circular card look
        cardPane.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 1000; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        double size = 400 * scale;
        cardPane.setPrefSize(size, size);
        cardPane.setMaxSize(size, size);

        for (String iconIdStr : iconIds) {
            int iconId = Integer.parseInt(iconIdStr);
            String emoji = (iconId > 0 && iconId < EMOJIS.length) ? EMOJIS[iconId] : "?";

            Text iconView = new Text(emoji);
            // Add colorful glow effect
            iconView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);");

            // Random Sizing
            double fontSize = (40 + random.nextInt(40)) * scale;
            iconView.setFont(Font.font(fontSize));

            // Random Positioning
            double radius = (size / 2) - (fontSize);
            double range = radius * 0.7;

            double x = (random.nextDouble() * 2 * range) - range;
            double y = (random.nextDouble() * 2 * range) - range;

            iconView.setTranslateX(x);
            iconView.setTranslateY(y);

            // Rotation
            iconView.setRotate(random.nextDouble() * 360);

            if (interactive) {
                iconView.getStyleClass().add("icon-button");
                iconView.setCursor(javafx.scene.Cursor.HAND);
                // Stronger hover effect handled in CSS, but we can add specific style here if
                // needed
                iconView.setOnMouseClicked(e -> {
                    lastClickX = e.getSceneX();
                    lastClickY = e.getSceneY();
                    client.sendMatchAttempt(iconId);
                });
            }

            cardPane.getChildren().add(iconView);
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: " + (24 * scale) + "px; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox cardBox = new VBox(15, titleLabel, cardPane);
        cardBox.setAlignment(Pos.CENTER);
        return cardBox;
    }
}
