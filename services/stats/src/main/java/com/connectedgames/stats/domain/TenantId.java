package com.connectedgames.stats.domain;

import java.util.Objects;

/** Organization that owns the venue, as the hub declared it: a string, not a UUID. */
public record TenantId(String value) {

    public TenantId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be blank");
        }
    }
}
