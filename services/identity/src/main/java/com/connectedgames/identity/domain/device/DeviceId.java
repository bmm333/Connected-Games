package com.connectedgames.identity.domain.device;

import java.util.Objects;
import java.util.UUID;

public record DeviceId(UUID value) {
    public DeviceId{
        Objects.requireNonNull(value,"value");
    }
}
