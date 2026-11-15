package com.connectedgames.match.domain.event;

import java.time.Instant;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;

/**
 * What the edge tells the cloud about a match. venueId and tableId travel with every event so that an
 * event can be checked against the match it claims to belong to; seq orders the events of one match.
 */
public sealed interface MatchEvent permits MatchStarted, GoalScored, MatchEnded {
    EventId id();
    VenueId venueId();
    TableId tableId();
    MatchId matchId();
    int seq();
    Instant occurredAt();
}
