package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.application.port.out.TableRepository;
import com.connectedgames.catalog.domain.table.QrCode;
import com.connectedgames.catalog.domain.table.Table;
import com.connectedgames.catalog.domain.venue.VenueId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Implements the TableRepository port with JPA. */
@Component
class TablePersistenceAdapter implements TableRepository {

    private final GameTableJpaRepository jpa;

    TablePersistenceAdapter(GameTableJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Table> findByCode(QrCode code) {
        return jpa.findByQrCode(code.value()).map(GameTableEntity::toDomain);
    }

    @Override
    public List<Table> findByVenue(VenueId venueId) {
        return jpa.findByVenueIdOrderByName(venueId.value()).stream().map(GameTableEntity::toDomain).toList();
    }
}
