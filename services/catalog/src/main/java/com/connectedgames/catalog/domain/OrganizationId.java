package com.connectedgames.catalog.domain;

import java.util.Objects;
import java.util.UUID;

/** The organization that owns the venues: it is the tenant. */
public record OrganizationId(UUID value)
{
    public OrganizationId{
        Objects.requireNonNull(value,"value");
    }
}
