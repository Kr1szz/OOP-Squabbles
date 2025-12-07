package com.squabbles.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Minimal player model.
 */
public class Player {
    public final String id;
    public final String username;
    public volatile int mmr;
    // connection placeholder (adapt to your actual Session type)
    public volatile Object connection;
    public volatile long queueJoinTime = 0L;
    public final AtomicLong lastSequence = new AtomicLong(0);
    public volatile long lastActionTimestamp = 0L;

    public Player(String id, String username, int mmr) {
        this.id = id;
        this.username = username;
        this.mmr = mmr;
    }

    @Override
    public String toString() {
        return "Player{" + id + "," + username + ",mmr=" + mmr + "}";
    }
}
