package com.connectedgames.identity.domain.user;

import java.util.UUID;
import java.util.Objects;

public record UserId(UUID value) {
    public UserId{
        Objects.requireNonNull(value,"value");
    }
}
