package com.connectedgames.stats.domain.stats;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.connectedgames.stats.domain.VenueId;

public record VenueSummary(VenueId venueId, int matches, int totalPoints, int averageDurationSeconds,
                           Instant lastMatchAt, List<VenueGameTypeTotals> byGameType) {
    public VenueSummary {
        Objects.requireNonNull(venueId, "venueId");
        if (matches < 0 || totalPoints < 0 || averageDurationSeconds < 0) {
            throw new IllegalArgumentException("A total cannot be negative");
        }
        byGameType = List.copyOf(byGameType);
        if (byGameType.stream().mapToInt(VenueGameTypeTotals::matches).sum() != matches) {
            throw new IllegalArgumentException("The game types must add up to the matches");
        }
    }
}
