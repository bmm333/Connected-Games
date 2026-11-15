package com.connectedgames.match.application.port.in;

import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.MatchId;

public interface GetMatchUseCase {

    Match getMatch(MatchId matchId);
}
