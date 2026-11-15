package com.connectedgames.match.application.port.in;

import java.util.Objects;

import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.MatchEvent;

/** The coordinates read from the MQTT topic: what the broker let this hub write to. */
public record EventTopic(VenueId venueId, TableId tableId, MatchId matchId) {

    public EventTopic {
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(matchId, "matchId");
    }

    /** equals, not ==: the ids in the topic and in the event are different objects with the same value. */
    public boolean matches(MatchEvent e) {
        return venueId.equals(e.venueId())
                && tableId.equals(e.tableId())
                && matchId.equals(e.matchId());
    }
}
