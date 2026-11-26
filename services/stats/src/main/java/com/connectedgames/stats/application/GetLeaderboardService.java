package com.connectedgames.stats.application;

import java.util.List;

import com.connectedgames.stats.application.port.in.GetLeaderboardUseCase;
import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.StatsFilter;

public final class GetLeaderboardService implements GetLeaderboardUseCase {
    private final StatsQueries queries;

    public GetLeaderboardService(StatsQueries queries) {
        this.queries = queries;
    }

    @Override
    public List<LeaderboardEntry> leaderboard(Viewer viewer, StatsFilter filter, int limit) {
        Page page = new Page(limit, 0);
        return queries.leaderboard(Access.tenantsOf(viewer), filter, page.limit());
    }
}
