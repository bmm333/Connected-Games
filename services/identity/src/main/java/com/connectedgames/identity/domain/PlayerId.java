package com.connectedgames.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Who plays, guest or user: matches and scores refer to this id, not to the account */
public record PlayerId(UUID value) {
    public PlayerId{
        Objects.requireNonNull(value,"value");
    }
}
