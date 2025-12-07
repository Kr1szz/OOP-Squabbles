package com.squabbles.network;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

public class LobbyServer {
    private Queue<ServerClient> matchmakingQueue = new LinkedList<>();
    private List<GameRoom> activeRooms = new ArrayList<>();
    private List<ServerClient> connectedClients = new ArrayList<>();

    public synchronized void addClient(ServerClient client) {
        connectedClients.add(client);
    }

    public synchronized void removeClient(ServerClient client) {
        connectedClients.remove(client);
        matchmakingQueue.remove(client);
        // Room cleanup is handled in GameRoom
    }

    public synchronized void joinQueue(ServerClient client) {
        if (matchmakingQueue.contains(client))
            return;

        System.out.println("Player " + client.getPlayerId() + " joined queue.");
        matchmakingQueue.add(client);
        client.sendMessage(NetworkProtocol.MSG_JOIN_QUEUE + " Waiting for opponent...");

        checkQueue();
    }

    private void checkQueue() {
        if (matchmakingQueue.size() >= 2) {
            ServerClient p1 = matchmakingQueue.poll();
            ServerClient p2 = matchmakingQueue.poll();

            createGameRoom(p1, p2);
        }
    }

    private void createGameRoom(ServerClient p1, ServerClient p2) {
        System.out.println("Match found: " + p1.getPlayerId() + " vs " + p2.getPlayerId());

        p1.sendMessage(NetworkProtocol.MSG_GAME_FOUND + " Player " + p2.getPlayerId());
        p2.sendMessage(NetworkProtocol.MSG_GAME_FOUND + " Player " + p1.getPlayerId());

        List<ServerClient> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        GameRoom room = new GameRoom(players);
        activeRooms.add(room);
        room.startGame();
    }

    public synchronized void startBotGame(ServerClient player, int difficulty) {
        System.out.println("Starting game for Player " + player.getPlayerId() + " with difficulty " + difficulty);

        List<ServerClient> players = new ArrayList<>();
        players.add(player);

        if (difficulty > 0) {
            // Create a bot
            BotClient bot = new BotClient(-1, this, difficulty); // ID -1 for bot
            players.add(bot);
        }

        GameRoom room = new GameRoom(players);
        activeRooms.add(room);
        room.startGame();
    }
}
