package com.connectedgames.stats.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.Totals;
import com.connectedgames.stats.domain.stats.VenueSummary;

/** Gives the answers it was told to give and remembers what it was asked. */
class StubStatsQueries implements StatsQueries {

    final List<String> calls = new ArrayList<>();
    Set<TenantId> lastTenants;
    Viewer lastViewer;
    StatsFilter lastFilter;
    Page lastPage;
    int lastLimit;

    List<LeaderboardEntry> leaderboard = List.of();
    boolean participantKnown = true;
    ParticipantSummary participantSummary;
    List<ParticipantMatch> participantMatches = List.of();
    Optional<MatchResult> matchResult = Optional.empty();
    boolean venueKnown = true;
    VenueSummary venueSummary;

    @Override
    public List<LeaderboardEntry> leaderboard(Set<TenantId> tenants, StatsFilter filter, int limit) {
        calls.add("leaderboard");
        lastTenants = tenants;
        lastFilter = filter;
        lastLimit = limit;
        return leaderboard;
    }

    @Override
    public boolean participantKnown(Viewer viewer, String participantId) {
        calls.add("participantKnown");
        lastViewer = viewer;
        return participantKnown;
    }

    @Override
    public ParticipantSummary participantSummary(Viewer viewer, String participantId, StatsFilter filter) {
        calls.add("participantSummary");
        lastViewer = viewer;
        lastFilter = filter;
        return participantSummary != null ? participantSummary
                : new ParticipantSummary(participantId, null, null, Totals.none(), List.of());
    }

    @Override
    public List<ParticipantMatch> participantMatches(Viewer viewer, String participantId, StatsFilter filter,
                                                     Page page) {
        calls.add("participantMatches");
        lastViewer = viewer;
        lastFilter = filter;
        lastPage = page;
        return participantMatches;
    }

    @Override
    public Optional<MatchResult> matchResult(Viewer viewer, MatchId matchId) {
        calls.add("matchResult");
        lastViewer = viewer;
        return matchResult;
    }

    @Override
    public boolean venueKnown(Set<TenantId> tenants, VenueId venueId) {
        calls.add("venueKnown");
        lastTenants = tenants;
        return venueKnown;
    }

    @Override
    public VenueSummary venueSummary(Set<TenantId> tenants, VenueId venueId, StatsFilter filter) {
        calls.add("venueSummary");
        lastTenants = tenants;
        lastFilter = filter;
        return venueSummary != null ? venueSummary : new VenueSummary(venueId, 0, 0, 0, null, List.of());
    }
}
