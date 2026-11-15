package com.connectedgames.match.domain;

import java.util.Objects;
import java.util.UUID;

public record EventId(UUID value) {
    public EventId { Objects.requireNonNull(value); }
}
