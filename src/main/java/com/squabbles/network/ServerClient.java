package com.squabbles.network;

import com.squabbles.model.Card;
import com.squabbles.model.Icon;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerClient implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private int playerId;
    private Card currentCard;
    private int score = 0;
    private LobbyServer lobby;
    private GameRoom gameRoom;
    private boolean connected = true;
    private long lastActionTime = 0;

    public ServerClient(Socket socket, int playerId, LobbyServer lobby) {
        this.socket = socket;
        this.playerId = playerId;
        this.lobby = lobby;
        if (socket != null) {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            sendMessage(NetworkProtocol.MSG_WELCOME + " " + playerId);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                processMessage(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private String playerName = "Unknown";

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    protected void processMessage(String message) {
        if (message.startsWith(NetworkProtocol.MSG_MATCH_ATTEMPT)) {
            if (gameRoom != null) {
                // Anti-Cheat: Cooldown
                long now = System.currentTimeMillis();
                if (now - lastActionTime < 200) { // 200ms cooldown
                    return;
                }
                lastActionTime = now;

                int iconId = Integer.parseInt(message.split(" ")[1]);
                gameRoom.handleMatch(this, iconId);
            }
        } else if (message.startsWith(NetworkProtocol.MSG_REQUEST_STATE)) {
            if (gameRoom != null) {
                gameRoom.sendState(this);
            }
        } else if (message.startsWith(NetworkProtocol.MSG_END_GAME)) {
            if (gameRoom != null) {
                gameRoom.endGame(this);
            }
        } else if (message.startsWith(NetworkProtocol.MSG_JOIN_QUEUE)) {
            // Parse name if available: JOIN_QUEUE Name
            String[] parts = message.split(" ", 2);
            if (parts.length > 1) {
                this.playerName = parts[1];
            }
            lobby.joinQueue(this);
        } else if (message.startsWith(NetworkProtocol.MSG_PLAY_BOT)) {
            // PLAY_BOT difficulty name
            String[] parts = message.split(" ");
            int difficulty = 1;
            if (parts.length > 1) {
                try {
                    difficulty = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    difficulty = 1;
                }
            }
            if (parts.length > 2) {
                this.playerName = parts[2];
            }
            lobby.startBotGame(this, difficulty);
        }
    }

    public void sendMessage(String message) {
        if (out != null)
            out.println(message);
    }

    public void sendState(Card centerCard) {
        // Format: UPDATE_CARDS [CenterCardId:Icon1,Icon2...]
        // [PlayerCardId:Icon1,Icon2...]
        StringBuilder sb = new StringBuilder(NetworkProtocol.MSG_UPDATE_CARDS);
        sb.append(" ");
        sb.append(serializeCard(centerCard));
        sb.append(" ");
        sb.append(serializeCard(currentCard));
        sendMessage(sb.toString());
    }

    private String serializeCard(Card card) {
        if (card == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        sb.append(card.getId()).append(":");
        for (Icon icon : card.getIcons()) {
            sb.append(icon.getId()).append(",");
        }
        return sb.toString();
    }

    public void disconnect() {
        if (!connected)
            return;
        connected = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (gameRoom != null) {
            gameRoom.removePlayer(this);
        }
        lobby.removeClient(this);
    }

    public int getPlayerId() {
        return playerId;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public void setCurrentCard(Card card) {
        this.currentCard = card;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    private int lives = 5;

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }
}
