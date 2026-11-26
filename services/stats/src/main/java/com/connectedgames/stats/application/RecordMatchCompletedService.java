package com.connectedgames.stats.application;

import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.application.port.in.RecordOutcome;
import com.connectedgames.stats.application.port.in.RecordMatchCompletedUseCase;

public final class RecordMatchCompletedService implements RecordMatchCompletedUseCase 
{
    private final MatchResultRepository results;
    public RecordMatchCompletedService(MatchResultRepository results){
        this.results=results;
    }
    @Override
    public RecordOutcome record(MatchResult result)
    {
        if(!result.counts())
        {
            return RecordOutcome.IGNORED;
        }
        return results.saveIfAbsent(result) ? RecordOutcome.RECORDED : RecordOutcome.DUPLICATE;
    }
}
