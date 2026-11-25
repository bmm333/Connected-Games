package com.connectedgames.stats.adapters.in.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectedgames.stats.application.port.in.RecordMatchCompletedUseCase;
import com.connectedgames.stats.application.port.in.RecordOutcome;
import com.connectedgames.stats.domain.result.MatchResult;

public class MatchCompletedListener {
    private static final Logger log = 
    LoggerFactory.getLogger(MatchCompletedListener.class);
    private final RecordMatchCompletedUseCase record;
    private final MatchCompletedParser parser= new MatchCompletedParser();

    public MatchCompletedListener(RecordMatchCompletedUseCase record)
    {
        this.record=record;
    }
    public void onMessage(String payload){
        MatchResult result;
        try{
            result=parser.parse(payload);
        } catch (UnreadableMessageException e){
            //broker would send it again and again 
            log.warn("MatchCompleted dropped: {}", e.getMessage());
            return;
        }
        RecordOutcome outcome=record.record(result);
        log.info("MatchCompleted of match {}: {}", result.id().value(),outcome);
        //others not caught. client disconect and broker send again.
    }
}
