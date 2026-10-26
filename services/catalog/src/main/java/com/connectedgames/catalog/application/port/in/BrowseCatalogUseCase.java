package com.connectedgames.catalog.application.port.in;

import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;

import java.util.List;
import java.util.Optional;


public interface BrowseCatalogUseCase {

    List<Venue> listVenues(Optional<String> city);
    Venue getVenue(VenueId id);
    List<Table> listTables(VenueId venueId);
    List<GameType> listGameTypes();
}
