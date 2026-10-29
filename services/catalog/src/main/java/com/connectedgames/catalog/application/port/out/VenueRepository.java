package com.connectedgames.catalog.application.port.out;

import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;

import java.util.List;
import java.util.Optional;

public interface VenueRepository {

    Optional<Venue> findById(VenueId id);
    List<Venue> findAll(Optional<String> city);
}
