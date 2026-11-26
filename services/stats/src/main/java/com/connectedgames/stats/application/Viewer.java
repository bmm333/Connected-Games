package com.connectedgames.stats.application;

import java.util.Objects;
import java.util.Set;

import com.connectedgames.stats.domain.TenantId;

public sealed interface Viewer {
    record OfTenants(Set<TenantId> tenants) implements Viewer {
        public OfTenants {
            tenants = Set.copyOf(tenants);
            if (tenants.isEmpty()) {
                throw new IllegalArgumentException("A viewer of tenants needs at least one tenant");
            }
        }
    }

    record OfPlayer(String playerId) implements Viewer {
        public OfPlayer {
            Objects.requireNonNull(playerId, "playerId");
            if (playerId.isBlank()) {
                throw new IllegalArgumentException("playerId cannot be blank");
            }
        }
    }
}
