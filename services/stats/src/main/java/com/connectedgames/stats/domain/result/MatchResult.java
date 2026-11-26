package com.connectedgames.stats.domain.result;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.connectedgames.stats.domain.EndReason;
import com.connectedgames.stats.domain.GameMode;
import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.MessageId;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.TableId;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.TournamentMatchId;
import com.connectedgames.stats.domain.VenueId;

/**
 * A closed match as the match service reported it: the fact every statistic is calculated from.
 * messageId is the id of the message, for the logs. tournamentMatchId is null for a friendly match,
 * winnerSide is null for a draw. The points can be fewer than the score: the score is the one that counts.
 */
public record MatchResult(MatchId id, MessageId messageId, TenantId tenantId, VenueId venueId, TableId tableId,
                          GameTypeId gameTypeId, TournamentMatchId tournamentMatchId, GameMode mode,
                          int pointsToWin, EndReason reason, Side winnerSide, Instant startedAt, Instant endedAt,
                          List<SideResult> sides, List<ScoredPoint> points) {

    public MatchResult {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(venueId, "venueId");
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(gameTypeId, "gameTypeId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(endedAt, "endedAt");
        if (pointsToWin < 1) {
            throw new IllegalArgumentException("pointsToWin starts at 1");
        }
        if (endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("The match ended before it started");
        }
        sides = List.copyOf(sides);
        points = List.copyOf(points);
        if (sides.size() != 2 || sides.get(0).side() == sides.get(1).side()) {
            throw new IllegalArgumentException("A match has one RED and one BLUE side");
        }
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).seq() <= points.get(i - 1).seq()) {
                throw new IllegalArgumentException("The points must be in increasing order of seq");
            }
        }
    }

    /** Cancelled matches are not counted in the statistics (ADR011). */
    public boolean counts() {
        return reason != EndReason.CANCELLED;
    }

    public int durationSeconds() {
        return (int) Duration.between(startedAt, endedAt).toSeconds();
    }

    public Outcome outcomeOf(Side side) {
        if (winnerSide == null) {
            return Outcome.DRAW;
        }
        return winnerSide == side ? Outcome.WIN : Outcome.LOSS;
    }
}
