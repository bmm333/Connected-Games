package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.out.DeviceRepository;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.DeviceId;
import com.connectedgames.identity.domain.device.Surface;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryDeviceRepository implements DeviceRepository {

    private final Map<DeviceId, Device> devices = new HashMap<>();

    @Override
    public void save(Device device) {
        devices.put(device.id(), device);
    }

    @Override
    public Optional<Device> findById(DeviceId id) {
        return Optional.ofNullable(devices.get(id));
    }

    @Override
    public Optional<Device> findByTable(UUID tableId, Surface surface) {
        return devices.values().stream()
                .filter(d -> d.tableId().equals(tableId) && d.surface() == surface).findFirst();
    }

    @Override
    public void delete(DeviceId id) {
        devices.remove(id);
    }

    int count() { return devices.size(); }
}
