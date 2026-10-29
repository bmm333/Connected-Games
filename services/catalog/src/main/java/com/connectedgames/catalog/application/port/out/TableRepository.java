package com.connectedgames.catalog.application.port.out;

import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.VenueId;

import java.util.List;
import java.util.Optional;

public interface TableRepository{
    Optional<Table> findByCode(QrCode code);
    List<Table> findByVenue(VenueId venueId);
}
