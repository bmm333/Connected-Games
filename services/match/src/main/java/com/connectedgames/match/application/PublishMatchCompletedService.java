package com.connectedgames.match.application;

import java.time.Clock;

import com.connectedgames.match.application.port.in.PublishMatchCompletedUseCase;
import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.application.port.out.MatchCompletedPublisher;

public final class PublishMatchCompletedService implements PublishMatchCompletedUseCase {

    static final int BATCH = 100;

    private final MatchCompletedOutbox outbox;
    private final MatchCompletedPublisher publisher;
    private final Clock clock;

    public PublishMatchCompletedService(MatchCompletedOutbox outbox, MatchCompletedPublisher publisher, Clock clock) {
        this.outbox = outbox;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    public int publishPending() {
        int sent = 0;
        for (MatchCompleted message : outbox.unpublished(BATCH)) {
            // If the broker refuses, the exception stops the loop: this message and the next ones stay, in order.
            publisher.publish(message);
            // If this fails after the publish, the message is sent again: at least once, consumers accept it.
            outbox.markPublished(message.eventId(), clock.instant());
            sent++;
        }
        return sent;
    }
}
