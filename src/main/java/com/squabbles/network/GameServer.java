package com.squabbles.network;

import com.squabbles.logic.DeckGenerator;
import com.squabbles.model.Card;
import com.squabbles.model.Deck;
import com.squabbles.model.Icon;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer implements Runnable {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private Deck<Card> deck;
    private Card centerCard;
    private boolean gameRunning = false;
    private ExecutorService pool = Executors.newFixedThreadPool(4);

    private int expectedPlayers;

    public GameServer(int port, int expectedPlayers) {
        this.port = port;
        this.expectedPlayers = expectedPlayers;
        List<Card> generatedCards = new DeckGenerator().generateDeck();
        this.deck = new Deck<>(generatedCards);
        this.deck.shuffle();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Wait for players
            while (clients.size() < expectedPlayers) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, clients.size() + 1);
                clients.add(client);
                pool.execute(client);
                System.out.println("Player " + client.playerId + " connected.");
                broadcast(NetworkProtocol.MSG_PLAYER_JOINED + " " + clients.size() + "/" + expectedPlayers);
            }

            startGame();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        gameRunning = true;
        // Deal initial cards
        // Center card is the first one
        centerCard = deck.draw();

        // Give each player a card (conceptually, we just send them their card and the
        // center card)
        // In this game, players have their own card and match against the center.
        // Or is there one center card for everyone?
        // "Two side-by-side VBox containers, housing the Player Card and the Center
        // Card."
        // Usually Dobble/Spot It has one center pile and players have their own pile.
        // Or players have one card and center has the pile.
        // Let's assume: Center Card is shared. Each player has a "Player Card".

        for (ClientHandler client : clients) {
            client.currentCard = deck.draw();
            client.sendState();
        }

        broadcast(NetworkProtocol.MSG_START_GAME);
    }

    private synchronized void handleMatch(int playerId, int iconId) {
        if (!gameRunning)
            return;

        // Check if the icon is on both the player's card and the center card
        ClientHandler player = clients.get(playerId - 1);
        boolean onPlayerCard = player.currentCard.getIcons().stream().anyMatch(i -> i.getId() == iconId);
        boolean onCenterCard = centerCard.getIcons().stream().anyMatch(i -> i.getId() == iconId);

        if (onPlayerCard && onCenterCard) {
            // Match found!
            // Player wins this round.
            // Old Center Card is discarded (or player's card becomes center, etc. depending
            // on rules).
            // Standard rule: Player places their card on center.
            // So Center Card = Player's Card.
            // Player draws new card from deck.

            centerCard = player.currentCard;
            if (!deck.isEmpty()) {
                player.currentCard = deck.draw();
            } else {
                // Game Over logic or empty deck handling
                gameRunning = false;
                broadcast(NetworkProtocol.MSG_GAME_OVER + " Player " + playerId);
                return;
            }

            player.score++;
            int level = (player.score / 10) + 1;
            if (level < 1)
                level = 1;

            player.sendMessage(NetworkProtocol.MSG_MATCH_RESULT + " true " + player.score + " " + level);

            // Update all clients with new center card
            // And update this player with new player card
            for (ClientHandler c : clients) {
                c.sendState();
            }

        } else {
            // Penalty
            player.score--; // Optional penalty
            if (player.score < -10) {
                gameRunning = false;
                broadcast(NetworkProtocol.MSG_GAME_OVER + " Player " + playerId + " lost (Score < -10)");
                return;
            }

            int level = (player.score / 10) + 1;
            if (level < 1)
                level = 1;

            player.sendMessage(NetworkProtocol.MSG_MATCH_RESULT + " false " + player.score + " " + level);
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private Scanner in;
        private int playerId;
        private Card currentCard;
        private int score = 0;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
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
            }
        }

        private void processMessage(String message) {
            if (message.startsWith(NetworkProtocol.MSG_MATCH_ATTEMPT)) {
                int iconId = Integer.parseInt(message.split(" ")[1]);
                handleMatch(playerId, iconId);
            } else if (message.startsWith(NetworkProtocol.MSG_REQUEST_STATE)) {
                sendState();
            } else if (message.startsWith(NetworkProtocol.MSG_END_GAME)) {
                gameRunning = false;
                broadcast(NetworkProtocol.MSG_GAME_OVER + " Player " + playerId + " ended the game.");
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void sendState() {
            // Send Center Card and Player Card
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
    }

    public int getPort() {
        return port;
    }
}
