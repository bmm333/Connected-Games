package com.connectedgames.match.domain;

import java.util.Objects;
import java.util.UUID;

public record VenueId(UUID value) {
    public VenueId { Objects.requireNonNull(value); }
}
