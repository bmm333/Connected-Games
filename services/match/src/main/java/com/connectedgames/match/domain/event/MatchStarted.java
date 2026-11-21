package com.connectedgames.match.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.GameTypeId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.TenantId;
import com.connectedgames.match.domain.TournamentMatchId;
import com.connectedgames.match.domain.VenueId;

/** tournamentMatchId is null for a friendly or offline match. */
public record MatchStarted(EventId id, VenueId venueId, TableId tableId, MatchId matchId,
                           int seq, Instant occurredAt,
                           TenantId tenantId, GameTypeId gameTypeId, TournamentMatchId tournamentMatchId,
                           GameMode mode, int pointsToWin, List<SideSetup> sides) implements MatchEvent {

    public MatchStarted {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(matchId, "matchId");
        if (seq < 1) {
            throw new IllegalArgumentException("seq starts at 1");
        }
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(gameTypeId, "gameTypeId");
        Objects.requireNonNull(mode, "mode");
        if (pointsToWin < 1) {
            throw new IllegalArgumentException("Points of a match should be at least 1 to win");
        }
        sides = List.copyOf(sides);
        if (sides.size() != 2 || sides.stream().map(SideSetup::side).distinct().count() != 2) {
            throw new IllegalArgumentException("A match has exactly one RED and one BLUE side");
        }
    }
}
