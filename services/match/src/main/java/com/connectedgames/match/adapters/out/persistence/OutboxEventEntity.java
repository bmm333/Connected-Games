package com.connectedgames.match.adapters.out.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="outbox_event")
class OutboxEventEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private UUID eventId;
    private String type;
    private UUID matchId;
    private String payload;
    private Instant publishedAt;

    protected OutboxEventEntity(){

    }
    OutboxEventEntity(UUID eventId, String type, UUID matchId, String payload) {
        this.eventId = eventId;
        this.type = type;
        this.matchId = matchId;
        this.payload = payload;
    }

    String payload() {
        return payload;
    }

}
