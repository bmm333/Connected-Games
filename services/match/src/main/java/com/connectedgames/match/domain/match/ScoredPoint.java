package com.connectedgames.match.domain.match;

import java.time.Instant;
import java.util.Objects;

import com.connectedgames.match.domain.Side;

/** A point that counted in a match: a goal in foosball. seq is the one of its GoalScored event. */
public record ScoredPoint(int seq, Side side, Instant occurredAt) {

    public ScoredPoint {
        if (seq < 1) {
            throw new IllegalArgumentException("seq starts at 1");
        }
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
