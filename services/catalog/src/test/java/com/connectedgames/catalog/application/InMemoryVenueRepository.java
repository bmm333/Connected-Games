package com.connectedgames.catalog.application;

import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.connectedgames.catalog.application.port.out.VenueRepository;

class InMemoryVenueRepository implements VenueRepository{
    private final List<Venue> venues=new ArrayList<>();
    void add(Venue venue) {venues.add(venue);}
    @Override
    public Optional<Venue> findById(VenueId id)
    {
        return venues.stream().filter(v -> v.id().equals(id)).findFirst();
    }
    @Override
    public List<Venue> findAll(Optional<String> city)
    {
        return venues.stream()
                .filter(v -> city.isEmpty() || v.city().equalsIgnoreCase(city.get()))
                .toList();
    }

}
