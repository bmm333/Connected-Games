package com.connectedgames.catalog.domain.table;

import com.connectedgames.catalog.domain.gametype.GameTypeId;
import com.connectedgames.catalog.domain.venue.VenueId;

import java.util.Objects;

public record Table(TableId id,VenueId venueId,String name,QrCode code,GameTypeId gameTypeId,TableStatus status) {
    public Table{
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(venueId,"venueId");
        Objects.requireNonNull(code,"code");
        Objects.requireNonNull(gameTypeId,"gameTypeId");
        Objects.requireNonNull(status,"status");
        if(name==null||name.isBlank()) throw new IllegalArgumentException("Name of table is mandatory");
    }

    public boolean isAvailable()
    {
        return status==TableStatus.FREE;
    }
}
