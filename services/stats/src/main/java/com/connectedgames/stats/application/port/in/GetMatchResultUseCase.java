package com.connectedgames.stats.application.port.in;

import java.util.Optional;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.result.MatchDetail;

public interface GetMatchResultUseCase {
    Optional<MatchDetail> matchResult(Viewer viewer, MatchId matchId);
}
