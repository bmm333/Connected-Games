package com.connectedgames.stats.domain.stats;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record ParticipantSummary(String participantId, String displayName,Instant lastMatchAt, Totals totals, List<GameTypeTotals> byGameType) {
    public ParticipantSummary{
        Objects.requireNonNull(participantId,"participantId");
        Objects.requireNonNull(totals,"totals");
        byGameType=List.copyOf(byGameType);
        if(byGameType.stream().mapToInt(game -> game.totals().matches()).sum() != totals.matches()){
            throw new IllegalArgumentException("The game types must add up to the matches");
        }
    }
}
