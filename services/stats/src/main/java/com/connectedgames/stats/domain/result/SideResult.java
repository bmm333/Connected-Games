package com.connectedgames.stats.domain.result;

import java.util.Objects;

import com.connectedgames.stats.domain.Side;

/** One side of a closed match. participantId is missing if the match was played without login. */
public record SideResult(Side side, String participantId, String displayName, int score) {

    public SideResult {
        Objects.requireNonNull(side, "side");
        if (participantId != null && participantId.isBlank()) {
            throw new IllegalArgumentException("participantId cannot be blank");
        }
        if (score < 0) {
            throw new IllegalArgumentException("score cannot be negative");
        }
    }
}
