package com.connectedgames.stats.application;

import java.util.Optional;

import com.connectedgames.stats.application.port.in.GetMatchResultUseCase;
import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.result.MatchDetail;

public final class GetMatchResultService implements GetMatchResultUseCase {
    private final StatsQueries queries;

    public GetMatchResultService(StatsQueries queries) {
        this.queries = queries;
    }

    @Override
    public Optional<MatchDetail> matchResult(Viewer viewer, MatchId matchId) {
        return queries.matchResult(viewer, matchId).map(MatchDetail::of);
    }
}
