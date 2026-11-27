package com.connectedgames.stats.domain.stats;

import java.util.Objects;

public record LeaderboardEntry(int rank,String participantId, String displayName,Totals totals) {
    public LeaderboardEntry{
        if(rank<1)
        {
            throw new IllegalArgumentException("rank starts at 1");   
        }
        Objects.requireNonNull(participantId,"participantId");
        Objects.requireNonNull(totals,"totals");
    }

}
