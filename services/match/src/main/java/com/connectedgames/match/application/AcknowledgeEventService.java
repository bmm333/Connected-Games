package com.connectedgames.match.application;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import com.connectedgames.match.application.port.in.AcknowledgeEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.Ack;
import com.connectedgames.match.application.port.out.AckPublisher;
import com.connectedgames.match.application.port.out.Rejection;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.VenueId;

public final class AcknowledgeEventService implements AcknowledgeEventUseCase {

    private final AckPublisher publisher;
    private final Clock clock;

    public AcknowledgeEventService(AckPublisher publisher, Clock clock) {
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    public void acknowledge(VenueId venueId, List<RecordResult> results) {
        if(results.isEmpty())
        {
            return;
        }
        List<EventId> acknowledged=new ArrayList<>();
        List<Rejection> rejected= new ArrayList<>();
        for(RecordResult result: results)
        {
            switch(result){
                case RecordResult.Accepted a -> acknowledged.add(a.eventId());
                case RecordResult.Duplicate d -> acknowledged.add(d.eventId());
                case RecordResult.Rejected r -> rejected.add(new Rejection(r.eventId(),r.reason()));
            }
        }
        publisher.publish(new Ack(venueId, acknowledged, rejected, clock.instant()));
    }
}
