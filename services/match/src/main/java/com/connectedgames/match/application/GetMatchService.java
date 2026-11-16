package com.connectedgames.match.application;

import com.connectedgames.match.application.port.in.GetMatchUseCase;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.match.Match;

public final class GetMatchService implements GetMatchUseCase {

    private final MatchRepository matches;

    public GetMatchService(MatchRepository matches) {
        this.matches = matches;
    }

    @Override
    public Match getMatch(MatchId matchId) {
        return matches.findById(matchId).orElseThrow(MatchNotFoundException::new);
    }
}
