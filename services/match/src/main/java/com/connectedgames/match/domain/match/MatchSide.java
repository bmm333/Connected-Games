package com.connectedgames.match.domain.match;

import java.util.Objects;

import com.connectedgames.match.domain.Side;

public record MatchSide(Side side, String participantId, String displayName, int score) {

    public MatchSide {
        Objects.requireNonNull(side, "side");
        if (score < 0) {
            throw new IllegalArgumentException("A score cannot be negative");
        }
    }
}
