package com.connectedgames.match.domain;

import java.util.Objects;

/** Organization that owns the venue, as declared by the hub in every event. */
public record TenantId(String value) {
    public TenantId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("A tenant id cannot be blank");
        }
    }
}
