package com.connectedgames.tournament.domain.match;

import java.time.Instant;
import java.util.Objects;

import com.connectedgames.tournament.domain.MatchId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;

public record Outcome(Side winnerSide, int redScore, int blueScore, ResultSource source, MatchId matchId,
                      Instant completedAt) {

    public Outcome {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(completedAt, "completedAt");
        if (redScore < 0 || blueScore < 0) {
            throw new IllegalArgumentException("A score cannot be negative");
        }
        if (source == ResultSource.MATCH && matchId == null) {
            throw new IllegalArgumentException("A result that came from a match needs the matchId");
        }
        if (source != ResultSource.MATCH && matchId != null) {
            throw new IllegalArgumentException("Only a result that came from a match has a matchId");
        }
        if (source == ResultSource.BYE && (winnerSide == null || redScore != 0 || blueScore != 0)) {
            throw new IllegalArgumentException("A bye has a winner and no score");
        }
    }

    public boolean isDraw() {
        return winnerSide == null;
    }
}
