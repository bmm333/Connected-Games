package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.MatchEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_event")
class MatchEventEntity {

    @Id
    private UUID eventId;
    private UUID venueId;
    private UUID tableId;
    private UUID matchId;
    private int seq;
    private String type;
    private Instant occurredAt;
    private String payload;

    protected MatchEventEntity() {
        // required by JPA
    }

    static MatchEventEntity from(MatchEvent event) {
        MatchEventEntity row = new MatchEventEntity();
        row.eventId = event.id().value();
        row.venueId = event.venueId().value();
        row.tableId = event.tableId().value();
        row.matchId = event.matchId().value();
        row.seq = event.seq();
        row.type = EventPayloads.typeOf(event);
        row.occurredAt = event.occurredAt();
        row.payload = EventPayloads.write(event);
        return row;
    }

    MatchEvent toDomain() {
        return EventPayloads.read(type, payload, new EventId(eventId), new VenueId(venueId), new TableId(tableId),
                new MatchId(matchId), seq, occurredAt);
    }
}
