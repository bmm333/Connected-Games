package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.device.Surface;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface DeviceJpaRepository extends JpaRepository<DeviceEntity, UUID> {

    Optional<DeviceEntity> findByTableIdAndSurface(UUID tableId, Surface surface);
}
