package com.connectedgames.tournament.domain;

import java.util.Objects;
import java.util.UUID;

public record GameTypeId(UUID value) {

    public GameTypeId {
        Objects.requireNonNull(value, "value");
    }
}
