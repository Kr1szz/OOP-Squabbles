package com.squabbles.network;

import com.squabbles.logic.DeckGenerator;
import com.squabbles.model.Card;
import com.squabbles.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private List<ServerClient> players;
    private Deck<Card> deck;
    private Card centerCard;
    private boolean gameRunning = false;
    private final int WIN_SCORE = 10;
    private final int LOSE_SCORE = -10;
    private int currentTurnIndex = 0;

    public GameRoom(List<ServerClient> players) {
        this.players = new ArrayList<>(players);
        List<Card> generatedCards = new DeckGenerator().generateDeck();
        this.deck = new Deck<>(generatedCards);
        this.deck.shuffle();

        for (ServerClient player : players) {
            player.setGameRoom(this);
            player.setScore(0);
        }
    }

    public void startGame() {
        gameRunning = true;
        centerCard = deck.draw();

        for (ServerClient player : players) {
            player.setCurrentCard(deck.draw());
            player.sendState(centerCard);
        }

        broadcast(NetworkProtocol.MSG_START_GAME);

        // Randomly pick starting player
        currentTurnIndex = (int) (Math.random() * players.size());
        broadcastTurn();

        // Start bots if any
        for (ServerClient player : players) {
            if (player instanceof BotClient) {
                ((BotClient) player).startBotLogic();
            }
        }
    }

    private java.util.Timer turnTimer;

    private void broadcastTurn() {
        if (!gameRunning)
            return;
        
        // Cancel previous timer
        if (turnTimer != null) {
            turnTimer.cancel();
        }

        ServerClient current = players.get(currentTurnIndex);
        broadcast(NetworkProtocol.MSG_TURN_UPDATE + " " + current.getPlayerId());

        // Start new timer (10 seconds)
        turnTimer = new java.util.Timer();
        turnTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                // Time's up! Skip turn.
                // We need to synchronize this to avoid race conditions with handleMatch
                synchronized (GameRoom.this) {
                    if (gameRunning && players.get(currentTurnIndex) == current) {
                        // Penalize player for running out of time
                        current.setLives(current.getLives() - 1);
                        if (current.getLives() <= 0) {
                            endGameWithLoser(current, "ran out of lives! DEFEAT!");
                            return;
                        }
                        current.sendMessage(NetworkProtocol.MSG_MATCH_RESULT + " false " + current.getScore() + " " + current.getLives() + " Time's up!");
                        nextTurn();
                    }
                }
            }
        }, 10000);
    }

    private void nextTurn() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        currentTurnIndex = (currentTurnIndex + 1) % players.size();
        broadcastTurn();
    }

    public synchronized void handleMatch(ServerClient player, int iconId) {
        if (!gameRunning)
            return;

        // Turn Check
        if (players.indexOf(player) != currentTurnIndex) {
            // Not your turn
            return;
        }
        
        // Cancel timer immediately upon action
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        boolean onPlayerCard = player.getCurrentCard().getIcons().stream().anyMatch(i -> i.getId() == iconId);
        boolean onCenterCard = centerCard.getIcons().stream().anyMatch(i -> i.getId() == iconId);

        if (onPlayerCard && onCenterCard) {
            centerCard = player.getCurrentCard();
            if (!deck.isEmpty()) {
                player.setCurrentCard(deck.draw());
            }

            player.setScore(player.getScore() + 1);

            if (player.getScore() >= WIN_SCORE) {
                endGameWithWinner(player, "reached " + WIN_SCORE + " points! VICTORY!");
                return;
            }

            if (deck.isEmpty()) {
                endGameWithWinner(player, "deck empty! VICTORY!");
                return;
            }

            // Format: MATCH_RESULT success score lives
            player.sendMessage(NetworkProtocol.MSG_MATCH_RESULT + " true " + player.getScore() + " " + player.getLives());
            broadcastState();
            nextTurn();
        } else {
            player.setLives(player.getLives() - 1);
            if (player.getLives() <= 0) {
                endGameWithLoser(player, "ran out of lives! DEFEAT!");
                return;
            }
            // Format: MATCH_RESULT success score lives
            player.sendMessage(NetworkProtocol.MSG_MATCH_RESULT + " false " + player.getScore() + " " + player.getLives());
            nextTurn();
        }
    }

    private void broadcastState() {
        for (ServerClient p : players) {
            p.sendState(centerCard);
        }
    }

    private void broadcast(String message) {
        for (ServerClient p : players) {
            p.sendMessage(message);
        }
    }

    public void sendState(ServerClient player) {
        player.sendState(centerCard);
    }

    public void endGame(ServerClient player) {
        gameRunning = false;
        if (turnTimer != null) turnTimer.cancel();
        
        // Update DB: Player left, so they lose? Or just void?
        // Let's count it as a loss for the quitter.
        com.squabbles.util.DatabaseManager.updateStats(player.getPlayerName(), false);
        
        broadcast(NetworkProtocol.MSG_GAME_OVER + " Player " + player.getPlayerName() + " ended the game.");
        closeRoom();
    }

    public void removePlayer(ServerClient player) {
        if (gameRunning) {
            gameRunning = false;
            if (turnTimer != null) turnTimer.cancel();
            
            // Opponent left, remaining player wins
            for (ServerClient p : players) {
                if (p != player) {
                     com.squabbles.util.DatabaseManager.updateStats(p.getPlayerName(), true);
                }
            }
             com.squabbles.util.DatabaseManager.updateStats(player.getPlayerName(), false);

            broadcast(NetworkProtocol.MSG_GAME_OVER + " The opponent fled in terror! You win by default!");
        }
        players.remove(player);
    }

    private void endGameWithWinner(ServerClient winner, String reason) {
        gameRunning = false;
        if (turnTimer != null) turnTimer.cancel();
        
        for (ServerClient p : players) {
            if (p == winner) {
                p.sendMessage(NetworkProtocol.MSG_GAME_OVER + " You win! " + reason);
                com.squabbles.util.DatabaseManager.updateStats(p.getPlayerName(), true);
            } else {
                p.sendMessage(NetworkProtocol.MSG_GAME_OVER + " You lost. Opponent " + reason);
                com.squabbles.util.DatabaseManager.updateStats(p.getPlayerName(), false);
            }
        }
        closeRoom();
    }

    private void endGameWithLoser(ServerClient loser, String reason) {
        gameRunning = false;
        if (turnTimer != null) turnTimer.cancel();
        
        for (ServerClient p : players) {
            if (p == loser) {
                p.sendMessage(NetworkProtocol.MSG_GAME_OVER + " You lost. " + reason);
                com.squabbles.util.DatabaseManager.updateStats(p.getPlayerName(), false);
            } else {
                p.sendMessage(NetworkProtocol.MSG_GAME_OVER + " You win! Opponent " + reason);
                com.squabbles.util.DatabaseManager.updateStats(p.getPlayerName(), true);
            }
        }       
        closeRoom();
    }

    private void closeRoom() {
        // Cleanup if needed
        for (ServerClient p : players) {
            p.setGameRoom(null);
            if (p instanceof BotClient) {
                ((BotClient) p).stopBotLogic();
            }
        }
    }
}
