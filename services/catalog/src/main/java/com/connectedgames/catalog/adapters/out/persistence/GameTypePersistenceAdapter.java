package com.connectedgames.catalog.adapters.out.persistence;

import com.connectedgames.catalog.application.port.out.GameTypeRepository;
import com.connectedgames.catalog.domain.gametype.GameType;
import com.connectedgames.catalog.domain.gametype.GameTypeId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Implements the GameTypeRepository port with JPA. */
@Component
class GameTypePersistenceAdapter implements GameTypeRepository {

    private final GameTypeJpaRepository jpa;

    GameTypePersistenceAdapter(GameTypeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<GameType> findById(GameTypeId id) {
        return jpa.findById(id.value()).map(GameTypeEntity::toDomain);
    }

    @Override
    public List<GameType> findAll() {
        return jpa.findAllByOrderByName().stream().map(GameTypeEntity::toDomain).toList();
    }
}
