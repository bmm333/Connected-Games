package com.connectedgames.stats.application;

import java.util.Set;

import com.connectedgames.stats.domain.TenantId;

final class Access {
    private Access() {
    }

    static Set<TenantId> tenantsOf(Viewer viewer) {
        return switch (viewer) {
            case Viewer.OfTenants t -> t.tenants();
            case Viewer.OfPlayer _ -> throw new NotAllowedException();
        };
    }

    static void mayReadParticipant(Viewer viewer, String participantId) {
        if (viewer instanceof Viewer.OfPlayer player && !player.playerId().equals(participantId)) {
            throw new NotAllowedException();
        }
    }
}
