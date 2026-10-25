package com.connectedgames.catalog.domain.venue;

import java.util.Objects;
import java.util.UUID;

public record VenueId(UUID value)
{
    public VenueId{
        Objects.requireNonNull(value,"value");
    }
}
