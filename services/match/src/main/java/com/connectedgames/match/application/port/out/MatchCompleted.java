package com.connectedgames.match.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.GameTypeId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.TenantId;
import com.connectedgames.match.domain.TournamentMatchId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchSide;

public record MatchCompleted(EventId eventId,TenantId tenantId,VenueId venueId,TableId tableId,MatchId matchId,
GameTypeId gameTypeId,TournamentMatchId tournamentMatchId,GameMode mode,int pointsToWin,Instant startedAt,Instant endedAt,
EndReason reason,Side winnerSide, List<MatchSide> sides)
{
    public MatchCompleted {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(gameTypeId, "gameTypeId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(endedAt, "endedAt");
        Objects.requireNonNull(reason, "reason");
        sides = List.copyOf(sides);
    }

    public static MatchCompleted of(EventId eventId, Match match) {
        if (!match.isClosed()) {
            throw new IllegalArgumentException("The match " + match.id().value() + " is still in progress");
        }
        return new MatchCompleted(eventId, match.tenantId(), match.venueId(), match.tableId(), match.id(),
                match.gameTypeId(), match.tournamentMatchId(), match.mode(), match.pointsToWin(),
                match.startedAt(), match.endedAt(), match.endReason(), match.winnerSide(), match.sides());
    }
}
