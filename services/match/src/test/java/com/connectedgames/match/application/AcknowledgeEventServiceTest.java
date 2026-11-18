package com.connectedgames.match.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.Ack;
import com.connectedgames.match.application.port.out.Rejection;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.VenueId;

import static org.assertj.core.api.Assertions.assertThat;

class AcknowledgeEventServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");
    private static final VenueId VENUE = new VenueId(UUID.randomUUID());

    private final FakeAckPublisher publisher = new FakeAckPublisher();
    private final AcknowledgeEventService service =
            new AcknowledgeEventService(publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    private static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    @Test
    void acceptedAndDuplicatesAreBothAcknowledged() {
        EventId accepted = newId();
        EventId duplicate = newId();

        service.acknowledge(VENUE, List.of(new RecordResult.Accepted(accepted), new RecordResult.Duplicate(duplicate)));

        Ack ack = publisher.published.getFirst();
        assertThat(ack.acknowledged()).containsExactly(accepted, duplicate);
        assertThat(ack.rejected()).isEmpty();
    }

    @Test
    void rejectedGoWithTheirReason() {
        EventId rejected = newId();

        service.acknowledge(VENUE, List.of(new RecordResult.Rejected(rejected, "seq 1 is already used")));

        Ack ack = publisher.published.getFirst();
        assertThat(ack.acknowledged()).isEmpty();
        assertThat(ack.rejected()).containsExactly(new Rejection(rejected, "seq 1 is already used"));
    }

    @Test
    void aMixedBatchIsSplit() {
        EventId accepted = newId();
        EventId rejected = newId();

        service.acknowledge(VENUE, List.of(new RecordResult.Accepted(accepted),
                new RecordResult.Rejected(rejected, "wrong topic")));

        Ack ack = publisher.published.getFirst();
        assertThat(ack.acknowledged()).containsExactly(accepted);
        assertThat(ack.rejected()).extracting(Rejection::eventId).containsExactly(rejected);
    }

    @Test
    void theAckIsForTheVenueGiven() {
        service.acknowledge(VENUE, List.of(new RecordResult.Accepted(newId())));

        assertThat(publisher.published.getFirst().venueId()).isEqualTo(VENUE);
    }

    @Test
    void processedAtComesFromTheClock() {
        service.acknowledge(VENUE, List.of(new RecordResult.Accepted(newId())));

        assertThat(publisher.published.getFirst().processedAt()).isEqualTo(NOW);
    }

    @Test
    void oneCallSendsOneAck() {
        service.acknowledge(VENUE, List.of(new RecordResult.Accepted(newId()), new RecordResult.Accepted(newId())));

        assertThat(publisher.published).hasSize(1);
    }

    @Test
    void nothingToAcknowledgeSendsNothing() {
        service.acknowledge(VENUE, List.of());

        assertThat(publisher.published).isEmpty();
    }
}
