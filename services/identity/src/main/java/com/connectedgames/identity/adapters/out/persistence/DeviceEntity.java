package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.DeviceId;
import com.connectedgames.identity.domain.device.Surface;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "device")
class DeviceEntity {

    @Id
    private UUID id;
    private UUID venueId;
    private UUID tableId;
    @Enumerated(EnumType.STRING)
    private Surface surface;
    private Instant pairedAt;

    protected DeviceEntity() {
        // required by JPA
    }

    static DeviceEntity from(Device device) {
        DeviceEntity row = new DeviceEntity();
        row.id = device.id().value();
        row.venueId = device.venueId();
        row.tableId = device.tableId();
        row.surface = device.surface();
        row.pairedAt = device.pairedAt();
        return row;
    }

    Device toDomain() {
        return new Device(new DeviceId(id), venueId, tableId, surface, pairedAt);
    }
}
