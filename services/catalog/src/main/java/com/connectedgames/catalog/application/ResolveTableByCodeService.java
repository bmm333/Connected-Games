package com.connectedgames.catalog.application;

import com.connectedgames.catalog.application.port.in.ResolveTableByCodeUseCase;
import com.connectedgames.catalog.application.port.in.TableLookup;
import com.connectedgames.catalog.application.port.out.GameTypeRepository;
import com.connectedgames.catalog.application.port.out.TableRepository;
import com.connectedgames.catalog.application.port.out.VenueRepository;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.Venue;

public final class ResolveTableByCodeService implements ResolveTableByCodeUseCase{

    private final TableRepository tables;
    private final VenueRepository venues;
    private final GameTypeRepository gameTypes;
    
    public ResolveTableByCodeService(TableRepository tables,VenueRepository venues,
            GameTypeRepository gameTypes){
    
        this.tables=tables;
        this.venues=venues;
        this.gameTypes=gameTypes;
    }

    @Override
    public TableLookup resolve(QrCode code)
    {
        Table table=tables.findByCode(code)
            .orElseThrow(() -> new NotFoundException("No table with code " + code.value()));
        Venue venue = venues.findById(table.venueId())
                .orElseThrow(() -> new IllegalStateException("Table " + table.id().value() + " references a missing venue"));
        GameType gameType = gameTypes.findById(table.gameTypeId())
                .orElseThrow(() -> new IllegalStateException("Table " + table.id().value() + " references a missing game type"));
        return new TableLookup(table, venue, gameType);
    }
}
