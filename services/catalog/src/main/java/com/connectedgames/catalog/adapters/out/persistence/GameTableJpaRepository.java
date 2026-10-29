package com.connectedgames.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface GameTableJpaRepository extends JpaRepository<GameTableEntity, UUID> {
    Optional<GameTableEntity> findByQrCode(String qrCode);
    List<GameTableEntity> findByVenueIdOrderByName(UUID venueId);
}
