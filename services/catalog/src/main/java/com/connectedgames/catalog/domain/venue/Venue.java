package com.connectedgames.catalog.domain.venue;

import com.connectedgames.catalog.domain.OrganizationId;

import java.util.Objects;

public record Venue(VenueId id,OrganizationId organizationId,String name,String address,String city,VenueType type) {
    public Venue{
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(organizationId,"organizationId");
        Objects.requireNonNull(type,"type");
        if(name==null || name.isBlank()) throw new IllegalArgumentException("Local Name is mandatory");
        if(city==null || city.isBlank()) throw new IllegalArgumentException("Local City Name is mandatory");
    }
}
