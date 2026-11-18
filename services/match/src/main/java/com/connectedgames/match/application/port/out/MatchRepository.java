package com.connectedgames.match.application.port.out;

import java.util.Optional;

import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;

public interface MatchRepository {

    Optional<Match> findById(MatchId id);
    /**
     * Like findById, and keeps the match locked until the end of the transaction: two events of the same
     * match are recorded one after the other, so none of them is lost. Needs a transaction.
     */
    Optional<Match> findByIdForUpdate(MatchId id);
    Optional<Match> findCurrentByTable(TableId tableId);
    void save(Match match);
}
