package com.connectedgames.stats.application.port.in;

import java.util.List;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.StatsFilter;

public interface GetLeaderboardUseCase {
    List<LeaderboardEntry> leaderboard(Viewer viewer, StatsFilter filter, int limit);
}
