package com.connectedgames.stats.domain.stats;

import java.util.Objects;

import com.connectedgames.stats.domain.GameTypeId;

public record VenueGameTypeTotals(GameTypeId gameTypeId, int matches, int totalPoints, int averageDurationSeconds) {
    public VenueGameTypeTotals {
        Objects.requireNonNull(gameTypeId, "gameTypeId");
        if (matches < 0 || totalPoints < 0 || averageDurationSeconds < 0) {
            throw new IllegalArgumentException("A total cannot be negative");
        }
    }
}
