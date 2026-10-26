package com.connectedgames.catalog.application;

import com.connectedgames.catalog.application.port.out.TableRepository;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.VenueId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class InMemoryTableRepository implements TableRepository{
    private final List<Table> tables=new ArrayList<>();
    void add(Table table) { tables.add(table); }
    @Override 
    public Optional<Table> findByCode(QrCode code)
    {
        return tables.stream().filter(t -> t.code().equals(code)).findFirst();
    }
    @Override 
    public List<Table> findByVenue(VenueId venueId)
    {
        return tables.stream().filter( t -> t.venueId().equals(venueId)).toList();
    }

}
