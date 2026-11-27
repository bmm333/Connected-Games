package com.connectedgames.identity.domain.device;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Device(DeviceId id,UUID  venueId,UUID organizationId,UUID tableId,Surface surface, Instant pairedAt) {
    public Device{
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(venueId,"venueId");
        Objects.requireNonNull(organizationId,"organizationId");
        Objects.requireNonNull(tableId,"tableId");
        Objects.requireNonNull(surface,"surface");
        Objects.requireNonNull(pairedAt,"pairedAt");
    }
}
