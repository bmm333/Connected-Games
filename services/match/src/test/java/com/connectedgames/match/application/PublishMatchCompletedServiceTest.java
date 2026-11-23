package com.connectedgames.match.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.Fixtures;
import com.connectedgames.match.application.port.out.MatchCompleted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishMatchCompletedServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T10:00:00Z");

    private final InMemoryMatchCompletedOutbox outbox = new InMemoryMatchCompletedOutbox();
    private final FakeMatchCompletedPublisher publisher = new FakeMatchCompletedPublisher();
    private final PublishMatchCompletedService service =
            new PublishMatchCompletedService(outbox, publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void publishesWhatIsWaitingInOrderAndMarksEachMessage() {
        MatchCompleted first = Fixtures.completed();
        MatchCompleted second = Fixtures.completed();
        outbox.add(first);
        outbox.add(second);

        int sent = service.publishPending();

        assertThat(sent).isEqualTo(2);
        assertThat(publisher.published).containsExactly(first, second);
        assertThat(outbox.published).containsOnlyKeys(first.eventId(), second.eventId());
        assertThat(outbox.published.get(first.eventId())).isEqualTo(NOW);
    }

    @Test
    void withNothingWaitingNothingIsSent() {
        assertThat(service.publishPending()).isZero();
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void aPublishedMessageIsNotSentAgain() {
        outbox.add(Fixtures.completed());
        service.publishPending();

        int again = service.publishPending();

        assertThat(again).isZero();
        assertThat(publisher.published).hasSize(1);
    }

    @Test
    void ifTheBrokerRefusesTheMessageAndTheNextOnesStayInTheOutboxInOrder() {
        MatchCompleted first = Fixtures.completed();
        MatchCompleted second = Fixtures.completed();
        MatchCompleted third = Fixtures.completed();
        outbox.add(first);
        outbox.add(second);
        outbox.add(third);
        publisher.refuseAfter = 1;

        assertThatThrownBy(service::publishPending).isInstanceOf(IllegalStateException.class);

        assertThat(outbox.published).containsOnlyKeys(first.eventId());
        assertThat(outbox.unpublished(10)).containsExactly(second, third);
    }

    @Test
    void afterAnOutageTheWaitingMessagesGoOutInTheirOrder() {
        MatchCompleted first = Fixtures.completed();
        MatchCompleted second = Fixtures.completed();
        outbox.add(first);
        outbox.add(second);
        publisher.refuseAfter = 0;
        assertThatThrownBy(service::publishPending).isInstanceOf(IllegalStateException.class);

        publisher.refuseAfter = -1;
        int sent = service.publishPending();

        assertThat(sent).isEqualTo(2);
        assertThat(publisher.published).containsExactly(first, second);
    }

    @Test
    void aPassSendsAtMostOneBatchTheNextPassSendsTheRest() {
        for (int i = 0; i < PublishMatchCompletedService.BATCH + 1; i++) {
            outbox.add(Fixtures.completed());
        }

        assertThat(service.publishPending()).isEqualTo(PublishMatchCompletedService.BATCH);
        assertThat(service.publishPending()).isEqualTo(1);
        assertThat(publisher.published).hasSize(PublishMatchCompletedService.BATCH + 1);
    }
}
