package com.connectedgames.match.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;

/**
 * winnerSide is null when the match is cancelled or ends in a draw. finalScore is what the edge counted:
 * it is the complete record, so it wins over the goals the cloud has received so far.
 */
public record MatchEnded(EventId id, VenueId venueId, TableId tableId, MatchId matchId,
                         int seq, Instant occurredAt,
                         EndReason reason, List<SideScore> finalScore, Side winnerSide) implements MatchEvent {

    public MatchEnded {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(matchId, "matchId");
        if (seq < 1) {
            throw new IllegalArgumentException("seq starts at 1");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(reason, "reason");
        finalScore = List.copyOf(finalScore);
        if (finalScore.stream().map(SideScore::side).distinct().count() < 2) {
            throw new IllegalArgumentException("The final score needs both sides");
        }
    }
}
