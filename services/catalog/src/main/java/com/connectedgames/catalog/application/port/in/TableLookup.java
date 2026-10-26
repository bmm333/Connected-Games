package com.connectedgames.catalog.application.port.in;

import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.Venue;


import java.util.Objects;


public record TableLookup(Table table,Venue venue, GameType gameType)
{
    public TableLookup{
        Objects.requireNonNull(table,"table");
        Objects.requireNonNull(venue,"venue");
        Objects.requireNonNull(gameType,"gameType");
    }
}
