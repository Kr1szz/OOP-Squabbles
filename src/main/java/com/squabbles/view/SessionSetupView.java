package com.squabbles.view;

import com.squabbles.Main;
import com.squabbles.network.GameClient;
import com.squabbles.network.GameServer;
import com.squabbles.network.NetworkProtocol;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class SessionSetupView {
    private Label statusLabel;
    private GameClient client;
    private GameServer server; // Keep reference to stop if needed, or just let it run
    private String playerName;

    public SessionSetupView(String playerName) {
        this.playerName = playerName;
    }

    public Scene getScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Label titleLabel = new Label("Session Setup");
        titleLabel.getStyleClass().add("header-label");

        // Host Section
        VBox hostBox = new VBox(10);
        hostBox.setAlignment(Pos.CENTER);
        hostBox.setStyle(
                "-fx-padding: 20; -fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10;");

        Label hostTitle = new Label("Host a Game");
        hostTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button hostButton = new Button("Start Server");
        Label localIpLabel = new Label("Local IP: Not Started");
        localIpLabel.getStyleClass().add("status-label");

        Label publicIpLabel = new Label("Public IP: (Click 'Start Server' to fetch)");
        publicIpLabel.getStyleClass().add("status-label");
        publicIpLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        Label portLabel = new Label("Port: -");
        portLabel.getStyleClass().add("status-label");

        Button copyLocalInfoButton = new Button("Copy Local Info");
        copyLocalInfoButton.getStyleClass().add("secondary-button");
        copyLocalInfoButton.setDisable(true);

        Button copyPublicInfoButton = new Button("Copy Public Info");
        copyPublicInfoButton.getStyleClass().add("secondary-button");
        copyPublicInfoButton.setDisable(true);

        HBox copyButtons = new HBox(10, copyLocalInfoButton, copyPublicInfoButton);
        copyButtons.setAlignment(Pos.CENTER);

        Label wanWarning = new Label(
                "For different WiFi/Networks, use Public IP.\nEnsure Port Forwarding is enabled on your router!");
        wanWarning.setTextAlignment(TextAlignment.CENTER);
        wanWarning.setStyle("-fx-font-size: 10px; -fx-text-fill: #ffaa00; -fx-font-style: italic;");

        hostBox.getChildren().addAll(hostTitle, hostButton, localIpLabel, publicIpLabel, portLabel, copyButtons,
                wanWarning);

        // Join Section
        VBox joinBox = new VBox(10);
        joinBox.setAlignment(Pos.CENTER);
        joinBox.setStyle(
                "-fx-padding: 20; -fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10;");

        Label joinTitle = new Label("Join a Game");
        joinTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField ipField = new TextField();
        ipField.setPromptText("Enter Host IP (e.g., 192.168.1.5)");
        ipField.setMaxWidth(250);

        TextField gameIdField = new TextField();
        gameIdField.setPromptText("Enter Port (e.g., 55555)");
        gameIdField.setMaxWidth(250);

        Button joinButton = new Button("Join Game");
        joinBox.getChildren().addAll(joinTitle, ipField, gameIdField, joinButton);

        statusLabel = new Label("Select an option...");
        statusLabel.getStyleClass().add("status-label");

        Button backButton = new Button("Back to Main Menu");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> {
            if (server != null) {
                server.stop();
            }
            if (client != null) {
                client.disconnect();
            }
            Main.setScene(new WelcomeView().getScene());
        });

        // Logic
        client = new GameClient();

        hostButton.setOnAction(e -> {
            // Start Server on a random high port to avoid "Address already in use"
            int port = generateRandomPort();
            server = new GameServer(port);
            new Thread(server).start();

            // Get Local IP
            String localIp = getLocalIpAddress();
            localIpLabel.setText("Local IP: " + localIp);
            portLabel.setText("Port: " + port);

            // Fetch Public IP in background
            new Thread(() -> {
                String publicIp = getPublicIpAddress();
                Platform.runLater(() -> publicIpLabel.setText("Public IP: " + publicIp));
            }).start();

            statusLabel.setText("Waiting for Player 2...");
            hostButton.setDisable(true);
            joinButton.setDisable(true); // Can't join if hosting in this simple UI
            copyLocalInfoButton.setDisable(false);
            copyPublicInfoButton.setDisable(false);

            // Connect as Player 1
            connectToServer("localhost", port);
        });

        copyLocalInfoButton.setOnAction(e -> {
            String text = "IP: " + localIpLabel.getText().replace("Local IP: ", "") + " | Port: "
                    + portLabel.getText().replace("Port: ", "");
            copyToClipboard(text);
            statusLabel.setText("Local Host details copied.");
        });

        copyPublicInfoButton.setOnAction(e -> {
            String text = "IP: " + publicIpLabel.getText().replace("Public IP: ", "") + " | Port: "
                    + portLabel.getText().replace("Port: ", "");
            copyToClipboard(text);
            statusLabel.setText("Public Host details copied. Ensure Port Forwarding!");
        });

        joinButton.setOnAction(e -> {
            String ip = ipField.getText();
            String idStr = gameIdField.getText();

            if (ip.isEmpty()) {
                statusLabel.setText("Please enter Host IP.");
                return;
            }
            if (idStr.isEmpty()) {
                statusLabel.setText("Please enter a Port.");
                return;
            }

            try {
                int port = Integer.parseInt(idStr);
                statusLabel.setText("Connecting to " + ip + "...");
                connectToServer(ip, port);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid Port.");
            }
        });

        root.getChildren().addAll(titleLabel, hostBox, new Label("- OR -"), joinBox, statusLabel, backButton);

        return new Scene(root, 800, 600);
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void connectToServer(String host, int port) {
        try {
            client.connect(host, port, this::handleMessage);
            // Auto-join queue upon connection for this simple UI
            client.sendMessage(NetworkProtocol.MSG_JOIN_QUEUE + " " + playerName);
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

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Check for site-local address (192.168.x.x, 10.x.x.x, etc.) and IPv4
                    if (addr.isSiteLocalAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
            // Fallback
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    private String getPublicIpAddress() {
        try {
            URL url = new URL("https://checkip.amazonaws.com");
            URLConnection con = url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            return br.readLine().trim();
        } catch (Exception e) {
            return "Unknown (Check manually)";
        }
    }

    private void handleMessage(String message) {
        if (message.startsWith(NetworkProtocol.MSG_WELCOME)) {
            String playerId = message.split(" ")[1];
            Platform.runLater(
                    () -> statusLabel.setText("Connected as Player " + playerId + ". Joined Queue..."));
        } else if (message.startsWith(NetworkProtocol.MSG_JOIN_QUEUE)) {
            Platform.runLater(
                    () -> statusLabel.setText(message.substring(NetworkProtocol.MSG_JOIN_QUEUE.length() + 1)));
        } else if (message.startsWith(NetworkProtocol.MSG_GAME_FOUND)) {
            Platform.runLater(() -> statusLabel.setText("Match Found! Starting..."));
        } else if (message.startsWith(NetworkProtocol.MSG_START_GAME)) {
            Platform.runLater(() -> {
                // Transition to GameView (true => multiplayer mode)
                Main.setScene(new GameView(client, true, playerName).getScene());
            });
        }
    }
}
