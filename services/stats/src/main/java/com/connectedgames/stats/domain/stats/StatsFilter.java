package com.connectedgames.stats.domain.stats;

import java.time.Instant;

import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.VenueId;

public record StatsFilter(VenueId venueId,GameTypeId gameTypeId,Instant from,Instant to) {
    public StatsFilter{
        if(from!=null && to != null && from.isAfter(to)){
            throw new IllegalArgumentException("from cannot be after to");
        }
    }
    public static StatsFilter none(){
        return new StatsFilter(null, null, null, null);
    }
}
