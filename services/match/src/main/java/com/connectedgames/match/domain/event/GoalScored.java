package com.connectedgames.match.domain.event;

import java.time.Instant;
import java.util.Objects;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;

public record GoalScored(EventId id, VenueId venueId, TableId tableId, MatchId matchId,
                         int seq, Instant occurredAt, Side side) implements MatchEvent {

    public GoalScored {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(matchId, "matchId");
        if (seq < 1) {
            throw new IllegalArgumentException("seq starts at 1");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(side, "side");
    }
}
