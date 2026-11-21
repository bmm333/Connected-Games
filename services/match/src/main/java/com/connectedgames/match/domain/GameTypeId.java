package com.connectedgames.match.domain;

import java.util.Objects;
import java.util.UUID;

/** Game type of catalog: no foreign key across services. */
public record GameTypeId(UUID value) {
    public GameTypeId { Objects.requireNonNull(value); }
}
