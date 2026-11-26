package com.connectedgames.stats.domain.result;

import java.time.Instant;
import java.util.Objects;

import com.connectedgames.stats.domain.Side;

/** A point that counted in a match: a goal in foosball. */
public record ScoredPoint(int seq, Side side, Instant occurredAt) {

    public ScoredPoint {
        if (seq < 1) {
            throw new IllegalArgumentException("seq starts at 1");
        }
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
