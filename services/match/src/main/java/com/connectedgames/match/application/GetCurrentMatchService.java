package com.connectedgames.match.application;

import com.connectedgames.match.application.port.in.GetCurrentMatchUseCase;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;

public final class GetCurrentMatchService implements GetCurrentMatchUseCase {

    private final MatchRepository matches;

    public GetCurrentMatchService(MatchRepository matches) {
        this.matches = matches;
    }

    @Override
    public Match getCurrentMatch(TableId tableId) {
        return matches.findCurrentByTable(tableId).orElseThrow(MatchNotFoundException::new);
    }
}
