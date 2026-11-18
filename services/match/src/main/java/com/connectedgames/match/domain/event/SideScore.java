package com.connectedgames.match.domain.event;

import java.util.Objects;

import com.connectedgames.match.domain.Side;

public record SideScore(Side side, int score) {

    public SideScore {
        Objects.requireNonNull(side, "side");
        if (score < 0) {
            throw new IllegalArgumentException("A score cannot be negative");
        }
    }
}
