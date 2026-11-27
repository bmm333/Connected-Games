package com.connectedgames.stats.application;

import java.util.Optional;
import java.util.Set;

import com.connectedgames.stats.application.port.in.GetVenueSummaryUseCase;
import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.VenueSummary;

public final class GetVenueSummaryService implements GetVenueSummaryUseCase {
    private final StatsQueries queries;

    public GetVenueSummaryService(StatsQueries queries) {
        this.queries = queries;
    }

    @Override
    public Optional<VenueSummary> venueSummary(Viewer viewer, VenueId venueId, StatsFilter filter) {
        Set<TenantId> tenants = Access.tenantsOf(viewer);
        if (!queries.venueKnown(tenants, venueId)) {
            return Optional.empty();
        }
        return Optional.of(queries.venueSummary(tenants, venueId, filter));
    }
}
