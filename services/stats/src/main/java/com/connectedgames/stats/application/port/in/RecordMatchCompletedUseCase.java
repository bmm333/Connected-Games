package com.connectedgames.stats.application.port.in;

public interface RecordMatchCompletedUseCase {

    /**Idempotent on the id of match. broker delivers atleast once*/
    RecordOutcome record(com.connectedgames.stats.domain.result.MatchResult result);
}
