package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.application.port.out.VenueRepository;
import com.connectedgames.catalog.domain.venue.Venue;
import com.connectedgames.catalog.domain.venue.VenueId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Implements the VenueRepository port with JPA. */
@Component
class VenuePersistenceAdapter implements VenueRepository {

    private final VenueJpaRepository jpa;

    VenuePersistenceAdapter(VenueJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Venue> findById(VenueId id) {
        return jpa.findById(id.value()).map(VenueEntity::toDomain);
    }

    @Override
    public List<Venue> findAll(Optional<String> city) {
        List<VenueEntity> rows = city
                .map(jpa::findByCityIgnoreCaseOrderByName)
                .orElseGet(jpa::findAllByOrderByName);
        return rows.stream().map(VenueEntity::toDomain).toList();
    }
}
