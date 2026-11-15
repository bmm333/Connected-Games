package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.application.port.out.GuestRepository;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implements the GuestRepository port with JPA. */
@Component
class GuestPersistenceAdapter implements GuestRepository {

    private final GuestJpaRepository jpa;

    GuestPersistenceAdapter(GuestJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Guest guest) {
        jpa.save(GuestEntity.from(guest));
    }

    @Override
    public Optional<Guest> findByPlayerId(PlayerId playerId) {
        return jpa.findById(playerId.value()).map(GuestEntity::toDomain);
    }
}
