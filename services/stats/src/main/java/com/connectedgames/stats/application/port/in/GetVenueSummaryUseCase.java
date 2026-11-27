package com.connectedgames.stats.application.port.in;

import java.util.Optional;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.VenueSummary;

public interface GetVenueSummaryUseCase {
    Optional<VenueSummary> venueSummary(Viewer viewer, VenueId venueId, StatsFilter filter);
}
