package server.model;

import java.util.Map;

/**
 * Represents an incoming client action.
 * Keep payload simple (Map) so server can inspect fields.
 */
public class ActionMessage {
    public final String playerId;
    public final long sequenceNumber;
    public final String actionType;
    public final Map<String, Object> payload;
    public final long clientTimestamp;

    public ActionMessage(String playerId, long sequenceNumber, String actionType, Map<String, Object> payload, long clientTimestamp) {
        this.playerId = playerId;
        this.sequenceNumber = sequenceNumber;
        this.actionType = actionType;
        this.payload = payload;
        this.clientTimestamp = clientTimestamp;
    }

    @Override
    public String toString() {
        return "ActionMessage{" + playerId + "," + actionType + "," + sequenceNumber + "}";
    }
}
