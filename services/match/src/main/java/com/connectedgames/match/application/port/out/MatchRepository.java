package com.connectedgames.match.application.port.out;

import java.util.Optional;

import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;

public interface MatchRepository {

    Optional<Match> findById(MatchId id);
    Optional<Match> findCurrentByTable(TableId tableId);
    void save(Match match);
}
