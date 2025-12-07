package com.squabbles.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal authoritative match state container.
 * Expand this to include units, towers, resources, etc.
 */
public class MatchState {
    public final String roomId;
    public volatile long startTime = 0L;
    public volatile int p1Health = 1000;
    public volatile int p2Health = 1000;
    public final Map<String, Object> entities = new ConcurrentHashMap<>(); // unitId -> state map
    public volatile int tickNumber = 0;

    public MatchState(String roomId) {
        this.roomId = roomId;
    }

    public boolean isGameOver() {
        return p1Health <= 0 || p2Health <= 0;
    }
}
