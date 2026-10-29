package com.connectedgames.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface GameTypeJpaRepository extends JpaRepository<GameTypeEntity, UUID> {
    List<GameTypeEntity> findAllByOrderByName();
}
