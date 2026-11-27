package com.connectedgames.stats.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.VenueSummary;

public interface StatsQueries {
    List<LeaderboardEntry> leaderboard(Set<TenantId> tenants, StatsFilter filter, int limit);

    boolean participantKnown(Viewer viewer, String participantId);

    ParticipantSummary participantSummary(Viewer viewer, String participantId, StatsFilter filter);

    List<ParticipantMatch> participantMatches(Viewer viewer, String participantId, StatsFilter filter, Page page);

    Optional<MatchResult> matchResult(Viewer viewer, MatchId matchId);

    boolean venueKnown(Set<TenantId> tenants, VenueId venueId);

    VenueSummary venueSummary(Set<TenantId> tenants, VenueId venueId, StatsFilter filter);
}
