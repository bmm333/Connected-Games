package com.connectedgames.match.application.port.out;

import java.time.Instant;
import java.util.List;

import com.connectedgames.match.domain.EventId;

public interface MatchCompleteOutbox {

    /**Wr the message in the transaction of the caller,it exists only if matche is closed*/
    void add(MatchCompleted message);
    /**Message not publuished yet. oldest first, most limit*/
    List<MatchCompleted> unpublished(int limit);
    /**Broker has the message: not sent again. row stays*/
    void markPublished(EventId id,Instant at);
}
