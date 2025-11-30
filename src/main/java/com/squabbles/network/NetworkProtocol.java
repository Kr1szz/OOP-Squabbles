package com.squabbles.network;

public class NetworkProtocol {
    public static final String MSG_WELCOME = "WELCOME"; // + playerId
    public static final String MSG_START_GAME = "START_GAME";
    public static final String MSG_UPDATE_CARDS = "UPDATE_CARDS"; // + card1Data + card2Data
    public static final String MSG_MATCH_ATTEMPT = "MATCH_ATTEMPT"; // + iconId
    public static final String MSG_MATCH_RESULT = "MATCH_RESULT"; // + success(boolean) + scoreUpdate
    public static final String MSG_GAME_OVER = "GAME_OVER"; // + winner
    public static final String MSG_PLAYER_JOINED = "PLAYER_JOINED";
    public static final String MSG_REQUEST_STATE = "REQUEST_STATE";
    public static final String MSG_END_GAME = "END_GAME";

    public static final int PORT = 55555; // Default, can be dynamic
}
