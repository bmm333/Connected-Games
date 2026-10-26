package com.connectedgames.catalog.application;
    
import java.util.List;
import java.util.Optional;

import com.connectedgames.catalog.application.port.in.BrowseCatalogUseCase;
import com.connectedgames.catalog.application.port.out.GameTypeRepository;
import com.connectedgames.catalog.application.port.out.TableRepository;
import com.connectedgames.catalog.application.port.out.VenueRepository;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;

public final class BrowseCatalogService  implements BrowseCatalogUseCase{
    private final VenueRepository venues;
    private final TableRepository tables;
    private final GameTypeRepository gameTypes;

    public BrowseCatalogService(VenueRepository venues,TableRepository tables,GameTypeRepository gameTypes)
    {
        this.venues=venues;
        this.tables=tables;
        this.gameTypes=gameTypes;
    }
    @Override 
    public List<GameType> listGameTypes()
    {
        return gameTypes.findAll();
    }
    @Override 
    public List<Venue> listVenues(Optional<String> city)
    {
        return venues.findAll(city);
    }
    @Override 
    public Venue getVenue(VenueId id)
    {
        return venues.findById(id)
            .orElseThrow(() -> new NotFoundException("Venue "+ id.value() + " does not exist"));
    }
    @Override 
    public List<Table> listTables(VenueId venueId)
    {
        getVenue(venueId);
        return tables.findByVenue(venueId);
    }
}










