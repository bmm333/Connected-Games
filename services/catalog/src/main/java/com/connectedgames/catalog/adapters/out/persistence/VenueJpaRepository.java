package com.connectedgames.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface VenueJpaRepository extends JpaRepository<VenueEntity, UUID> {
    List<VenueEntity> findAllByOrderByName();
    List<VenueEntity> findByCityIgnoreCaseOrderByName(String city);
}
