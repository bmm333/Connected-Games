package com.connectedgames.match.application.port.in;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.GoalScored;

import static org.assertj.core.api.Assertions.assertThat;

class EventTopicTest {

    private static final UUID VENUE = UUID.randomUUID();
    private static final UUID TABLE = UUID.randomUUID();
    private static final UUID MATCH = UUID.randomUUID();

    private final EventTopic topic = new EventTopic(new VenueId(VENUE), new TableId(TABLE), new MatchId(MATCH));

    private static GoalScored goal(UUID venue, UUID table, UUID match) {
        return new GoalScored(new EventId(UUID.randomUUID()), new VenueId(venue), new TableId(table),
                new MatchId(match), 1, Instant.parse("2026-11-15T20:00:00Z"), Side.RED);
    }

    @Test
    void matchesTheSameValuesEvenInDifferentObjects() {
        assertThat(topic.matches(goal(VENUE, TABLE, MATCH))).isTrue();
    }

    @Test
    void anotherVenueDoesNotMatch() {
        assertThat(topic.matches(goal(UUID.randomUUID(), TABLE, MATCH))).isFalse();
    }

    @Test
    void anotherTableDoesNotMatch() {
        assertThat(topic.matches(goal(VENUE, UUID.randomUUID(), MATCH))).isFalse();
    }

    @Test
    void anotherMatchDoesNotMatch() {
        assertThat(topic.matches(goal(VENUE, TABLE, UUID.randomUUID()))).isFalse();
    }
}
