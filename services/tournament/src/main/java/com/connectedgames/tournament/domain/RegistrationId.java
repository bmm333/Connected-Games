package com.connectedgames.tournament.domain;

import java.util.Objects;
import java.util.UUID;

public record RegistrationId(UUID value) {

    public RegistrationId {
        Objects.requireNonNull(value, "value");
    }
}
