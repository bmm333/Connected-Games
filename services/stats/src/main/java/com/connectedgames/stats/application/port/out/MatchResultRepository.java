package com.connectedgames.stats.application.port.out;

import com.connectedgames.stats.domain.result.MatchResult;

public interface MatchResultRepository {

    /**
     *  Saves the result with its sides and points, all or nothing. if the match is already saved
     *  it does nothing
     *
     *  @return true if it was saved now , else false.
     * */
    boolean saveIfAbsent(MatchResult result);
}
