package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.application.port.out.DeviceRepository;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.DeviceId;
import com.connectedgames.identity.domain.device.Surface;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Implements the DeviceRepository port with JPA. */
@Component
class DevicePersistenceAdapter implements DeviceRepository {

    private final DeviceJpaRepository jpa;

    DevicePersistenceAdapter(DeviceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Device device) {
        jpa.save(DeviceEntity.from(device));
    }

    @Override
    public Optional<Device> findById(DeviceId id) {
        return jpa.findById(id.value()).map(DeviceEntity::toDomain);
    }

    @Override
    public Optional<Device> findByTable(UUID tableId, Surface surface) {
        return jpa.findByTableIdAndSurface(tableId, surface).map(DeviceEntity::toDomain);
    }

    @Override
    public void delete(DeviceId id) {
        jpa.deleteById(id.value());
        jpa.flush();
    }
}
