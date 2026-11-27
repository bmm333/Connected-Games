package com.connectedgames.stats.adapters.in.web;

import java.util.List;
import java.util.Optional;

import com.connectedgames.stats.application.NotAllowedException;
import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.application.port.in.GetLeaderboardUseCase;
import com.connectedgames.stats.application.port.in.GetMatchResultUseCase;
import com.connectedgames.stats.application.port.in.GetParticipantMatchesUseCase;
import com.connectedgames.stats.application.port.in.GetParticipantSummaryUseCase;
import com.connectedgames.stats.application.port.in.GetVenueSummaryUseCase;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchDetail;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.VenueSummary;

/** The five use cases with the answers a test gives them, remembering what they were asked. */
class StubUseCases implements GetLeaderboardUseCase, GetParticipantSummaryUseCase, GetParticipantMatchesUseCase,
        GetMatchResultUseCase, GetVenueSummaryUseCase {

    Viewer lastViewer;
    StatsFilter lastFilter;
    Page lastPage;
    int lastLimit;
    String lastParticipantId;
    boolean forbidden;

    List<LeaderboardEntry> leaderboard = List.of();
    Optional<ParticipantSummary> summary = Optional.empty();
    Optional<List<ParticipantMatch>> matches = Optional.empty();
    Optional<MatchDetail> matchResult = Optional.empty();
    Optional<VenueSummary> venueSummary = Optional.empty();

    void reset() {
        lastViewer = null;
        lastFilter = null;
        lastPage = null;
        lastLimit = 0;
        lastParticipantId = null;
        forbidden = false;
        leaderboard = List.of();
        summary = Optional.empty();
        matches = Optional.empty();
        matchResult = Optional.empty();
        venueSummary = Optional.empty();
    }

    private void ask(Viewer viewer, StatsFilter filter) {
        lastViewer = viewer;
        lastFilter = filter;
        if (forbidden) {
            throw new NotAllowedException();
        }
    }

    @Override
    public List<LeaderboardEntry> leaderboard(Viewer viewer, StatsFilter filter, int limit) {
        lastLimit = limit;
        ask(viewer, filter);
        return leaderboard;
    }

    @Override
    public Optional<ParticipantSummary> summary(Viewer viewer, String participantId, StatsFilter filter) {
        lastParticipantId = participantId;
        ask(viewer, filter);
        return summary;
    }

    @Override
    public Optional<List<ParticipantMatch>> matches(Viewer viewer, String participantId, StatsFilter filter,
                                                    Page page) {
        lastParticipantId = participantId;
        lastPage = page;
        ask(viewer, filter);
        return matches;
    }

    @Override
    public Optional<MatchDetail> matchResult(Viewer viewer, MatchId matchId) {
        ask(viewer, null);
        return matchResult;
    }

    @Override
    public Optional<VenueSummary> venueSummary(Viewer viewer, VenueId venueId, StatsFilter filter) {
        ask(viewer, filter);
        return venueSummary;
    }
}
