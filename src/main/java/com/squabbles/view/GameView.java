package com.squabbles.view;

import com.squabbles.Main;
import com.squabbles.network.GameClient;
import com.squabbles.network.NetworkProtocol;
import com.squabbles.util.IconLoader;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;

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
    private final boolean multiplayer;

    private StackPane root;
    private VBox gameLayout;
    private Pane feedbackLayer;
    private HBox topBar;
    private HBox cardsContainer;
    private Label scoreLabel;
    private Label messageLabel;
    private Random random = new Random();

    private double lastClickX = 0;
    private double lastClickY = 0;

    private long lastMatchTime = 0;
    private int comboCount = 0;

    // Per-turn click lock: one attempt at a time per player
    private boolean canClick = true;

    private static final String MULTI_INSTRUCTION =
            "Your turn! First to 25 points wins. Find the matching icon.";
    private static final String SINGLE_INSTRUCTION = "Match as many icons as you can!";

    public GameView(GameClient client, boolean multiplayer) {
        this.client = client;
        this.multiplayer = multiplayer;
    }

    public Scene getScene() {
        // Initialize UI components
        topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.getStyleClass().add("top-bar");
        // End Game button with CSS class
        javafx.scene.control.Button endButton = new javafx.scene.control.Button("End Game");
        endButton.getStyleClass().add("end-game-button");
        endButton.setOnAction(e -> client.sendEndGame());
        // Score label with CSS class
        scoreLabel = new Label("Score: 0");
        scoreLabel.getStyleClass().add("score-label");
        // Message label with CSS class
        String initialText = multiplayer ? MULTI_INSTRUCTION : SINGLE_INSTRUCTION;
        messageLabel = new Label(initialText);
        messageLabel.getStyleClass().add("message-label");
        // Assemble top bar
        topBar.getChildren().addAll(endButton, scoreLabel, messageLabel);

        // Cards container
        cardsContainer = new HBox(50);
        cardsContainer.setAlignment(Pos.CENTER);

        // Layout assembly
        gameLayout = new VBox(20);
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.getChildren().addAll(topBar, cardsContainer);

        // Feedback Layer (Transparent overlay)
        feedbackLayer = new Pane();
        feedbackLayer.setPickOnBounds(false); // Allow clicks to pass through

        // Root assembly
        root = new StackPane();
        root.getStyleClass().add("root");
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
            // MSG_MATCH_RESULT success score
            String[] parts = message.split(" ");
            boolean success = Boolean.parseBoolean(parts[1]);
            int score = Integer.parseInt(parts[2]);

            Platform.runLater(() -> {
                scoreLabel.setText("Score: " + score);

                if (success) {
                    messageLabel.setText("Correct! +1");
                    messageLabel.setTextFill(Color.LIMEGREEN);
                    showFloatingFeedback(lastClickX, lastClickY, "+1", Color.LIMEGREEN);

                    // Combo Logic
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastMatchTime < 3000) { // 3 seconds window
                        comboCount++;
                        if (comboCount > 1) {
                            showFloatingFeedback(lastClickX, lastClickY - 50, "Combo x" + comboCount + "!", Color.GOLD);
                        }
                    } else {
                        comboCount = 1;
                    }
                    lastMatchTime = currentTime;
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
                            String base = multiplayer ? MULTI_INSTRUCTION : SINGLE_INSTRUCTION;
                            messageLabel.setText(base);
                            messageLabel.setTextFill(Color.WHITE);
                        });
                    }
                }, 1000);

                // After result, allow the player to act again (one attempt at a time)
                canClick = true;
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

        // Precompute layout parameters to minimise overlap
        int iconCount = iconIds.length;
        double nodeSize = 60 * scale;
        double maxRadius = (size / 2) - nodeSize;

        for (int index = 0; index < iconIds.length; index++) {
            String iconIdStr = iconIds[index];
            int iconId = Integer.parseInt(iconIdStr);

            Node iconNode;
            Image iconImage = IconLoader.getInstance().loadIcon(iconId);

            // Palette for per-icon base glow colours.
            // We derive the color from iconId so the SAME icon looks the same on every card.
            String[] baseColors = {
                    "#f5a97f", "#8aadf4", "#a6da95", "#f5bde6",
                    "#c6a0f6", "#eed49f", "#f0c6c6", "#7dc4e4"
            };
            String baseColor = baseColors[Math.floorMod(iconId, baseColors.length)];

            if (iconImage != null) {
                ImageView imageView = new ImageView(iconImage);
                // Scale image relative to card size
                double imgSize = (40 + random.nextInt(40)) * scale;
                imageView.setFitWidth(imgSize);
                imageView.setFitHeight(imgSize);
                imageView.setPreserveRatio(true);

                // Add colorful glow effect per icon
                imageView.setStyle("-fx-effect: dropshadow(gaussian, " + baseColor + ", 8, 0.7, 0, 0);");
                iconNode = imageView;
            } else {
                String emoji = IconLoader.getEmojiFallback(iconId);
                Text iconText = new Text(emoji);

                // Random Sizing
                double fontSize = (40 + random.nextInt(40)) * scale;
                iconText.setFont(Font.font(fontSize));

                // Per-emoji colorful glow effect
                iconText.setStyle("-fx-effect: dropshadow(gaussian, " + baseColor + ", 8, 0.7, 0, 0);");
                iconNode = iconText;
            }

            // Evenly-spaced polar positioning to avoid heavy overlap
            double angle = (2 * Math.PI * index) / iconCount;
            // Use 2 concentric rings if there are many icons
            double ringFactor = (iconCount > 6 && index % 2 == 0) ? 0.5 : 0.8;
            double radius = maxRadius * ringFactor;

            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);

            iconNode.setTranslateX(x);
            iconNode.setTranslateY(y);

            // Slight random rotation for a more playful look
            iconNode.setRotate(random.nextDouble() * 360);

            if (interactive) {
                iconNode.getStyleClass().add("icon-button");
                iconNode.setCursor(javafx.scene.Cursor.HAND);

                // Per-icon colorful hover effect (different color for each icon)
                String[] hoverColors = {
                        "#f5a97f", "#8aadf4", "#a6da95", "#f5bde6", "#c6a0f6", "#eed49f", "#f0c6c6"
                };
                String hoverColor = hoverColors[random.nextInt(hoverColors.length)];
                String baseStyle = iconNode.getStyle() == null ? "" : iconNode.getStyle();

                iconNode.setOnMouseEntered(e -> {
                    iconNode.setStyle(baseStyle +
                            "-fx-effect: dropshadow(three-pass-box, " + hoverColor + ", 20, 0, 0, 0);" +
                            "-fx-scale-x: 1.08; -fx-scale-y: 1.08;");
                });

                iconNode.setOnMouseExited(e -> {
                    iconNode.setStyle(baseStyle);
                });

                iconNode.setOnMouseClicked(e -> {
                    if (!canClick) {
                        // Ignore extra clicks until the server responds
                        return;
                    }
                    canClick = false;
                    lastClickX = e.getSceneX();
                    lastClickY = e.getSceneY();
                    if (multiplayer) {
                        messageLabel.setText("Waiting for opponent...");
                    } else {
                        messageLabel.setText("Checking your move...");
                    }
                    messageLabel.setTextFill(Color.LIGHTBLUE);
                    client.sendMatchAttempt(iconId);
                });
            }

            cardPane.getChildren().add(iconNode);
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: " + (24 * scale) + "px; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox cardBox = new VBox(15, titleLabel, cardPane);
        cardBox.setAlignment(Pos.CENTER);
        return cardBox;
    }
}
