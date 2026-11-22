package com.connectedgames.match.infrastructure;

import com.connectedgames.match.application.port.in.PublishMatchCompletedUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Sends what is waiting in the outbox, one pass after the other. A pass that fails (broker down) is tried again
 * at the next one: the messages are still in the table. One instance of the service only, as ADR011 says.
 */
@Configuration
@EnableScheduling
class OutboxPublisherSchedule {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherSchedule.class);

    private final PublishMatchCompletedUseCase publish;

    OutboxPublisherSchedule(PublishMatchCompletedUseCase publish) {
        this.publish = publish;
    }

    @Scheduled(fixedDelayString = "${match.outbox.poll-ms:1000}")
    void publishPending() {
        try {
            int sent = publish.publishPending();
            if (sent > 0) {
                log.info("{} MatchCompleted published", sent);
            }
        } catch (RuntimeException e) {
            log.warn("MatchCompleted not published, it will be tried again: {}", e.getMessage());
        }
    }
}
