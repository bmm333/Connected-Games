package com.connectedgames.match.adapters.in.mqtt;

import com.connectedgames.match.domain.EventId;

public class UnreadableMessageException extends RuntimeException { 

    private final transient EventId eventId;

    UnreadableMessageException(EventId eventId,String reason)
    {
        super(reason);
        this.eventId=eventId;
    }
    EventId eventId(){
        return eventId;
    }
}
