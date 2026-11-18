package com.connectedgames.identity.application.port.out;

import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.DeviceId;
import com.connectedgames.identity.domain.device.Surface;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository {

    void save(Device device);

    Optional<Device> findById(DeviceId id);

    Optional<Device> findByTable(UUID tableId, Surface surface);

    /** Done in the database when it returns, so the table is free for the next device */
    void delete(DeviceId id);
}
