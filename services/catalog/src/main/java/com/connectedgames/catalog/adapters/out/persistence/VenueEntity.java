package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.domain.OrganizationId;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;
import com.connectedgames.catalog.domain.venue.VenueType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name="venue")
class VenueEntity {
    @Id
    private UUID id;
    private UUID organizationId;
    private String name;
    private String address;
    private String city;
    @Enumerated(EnumType.STRING)
    private VenueType type;

    protected VenueEntity(){

    }
    Venue toDomain()
    {
        return new Venue(new VenueId(id),new OrganizationId(organizationId),name,address,city,type);
    }
}
