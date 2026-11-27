package com.connectedgames.stats.domain.stats;

import java.time.Instant;
import java.util.Objects;

import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.TableId;
import com.connectedgames.stats.domain.TournamentMatchId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.Outcome;



public record ParticipantMatch(MatchId matchId,VenueId venueId,TableId tableId,GameTypeId gameTypeId, TournamentMatchId tournamentMatchId, Instant endedAt,int durationSeconds,Side side,Outcome result,int pointsFor,int pointsAgainst,String opponentParticipantId,String opponentDisplayName) {
    public ParticipantMatch {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(gameTypeId, "gameTypeId");
        Objects.requireNonNull(endedAt, "endedAt");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(result, "result");
        if (durationSeconds < 0 || pointsFor < 0 || pointsAgainst < 0) {
            throw new IllegalArgumentException("A duration or a score cannot be negative");
        }
    }
}
