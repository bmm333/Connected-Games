package com.connectedgames.match.adapters.out.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.domain.EventId;

@Component
public class OutboxPersistenceAdapter implements MatchCompletedOutbox{
    private final OutboxEventJpaRepository jpa;
    OutboxPersistenceAdapter(OutboxEventJpaRepository jpa){
        this.jpa=jpa;
    }
    @Override
    public void add(MatchCompleted message)
    {
        if(jpa.existsByTypeAndMatchId(OutboxPayloads.MATCH_COMPLETED, message.matchId().value())){
            return;
        }
        jpa.save(new OutboxEventEntity(message.eventId().value(),OutboxPayloads.MATCH_COMPLETED,message.matchId().value(),OutboxPayloads.write(message)));
    }
    @Override
    public List<MatchCompleted> unpublished(int limit){
        return jpa.findByPublishedAtIsNullOrderBySeqAsc(PageRequest.of(0,limit)).stream().map(row -> OutboxPayloads.read(row.payload())).toList();
    }
    @Override
    public void markPublished(EventId id,Instant at){
        jpa.markPublished(id.value(), at);
    }
}





