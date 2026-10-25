package com.connectedgames.catalog.domain.table;

import java.util.Objects;
import java.util.UUID;

public record TableId(UUID value)
{
    public TableId{
        Objects.requireNonNull(value,"value");
    }
}
