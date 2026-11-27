package com.connectedgames.stats.domain.result;

import java.util.Objects;

/** A point with the score of the match right after it. */
public record ScoreAfterPoint(ScoredPoint point, int redScore, int blueScore) {

    public ScoreAfterPoint {
        Objects.requireNonNull(point, "point");
        if (redScore < 0 || blueScore < 0) {
            throw new IllegalArgumentException("A score cannot be negative");
        }
    }
}
