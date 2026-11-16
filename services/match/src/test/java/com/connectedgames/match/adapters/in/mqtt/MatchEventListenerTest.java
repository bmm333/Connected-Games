package com.connectedgames.match.adapters.in.mqtt;

import com.connectedgames.match.application.port.in.AcknowledgeEventUseCase;
import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.MatchEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.match.adapters.in.mqtt.MatchEventParserTest.EVENT;
import static com.connectedgames.match.adapters.in.mqtt.MatchEventParserTest.MATCH;
import static com.connectedgames.match.adapters.in.mqtt.MatchEventParserTest.TABLE;
import static com.connectedgames.match.adapters.in.mqtt.MatchEventParserTest.VENUE;
import static com.connectedgames.match.adapters.in.mqtt.MatchEventParserTest.envelope;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchEventListenerTest {

    private static final String TOPIC = "venues/%s/tables/%s/matches/%s/events".formatted(VENUE, TABLE, MATCH);
    private static final String GOAL = envelope("GoalScored", 2, "{\"side\":\"RED\"}");

    private final List<String> calls = new ArrayList<>();
    private final List<EventTopic> topics = new ArrayList<>();
    private final List<VenueId> acknowledgedVenues = new ArrayList<>();
    private final List<List<RecordResult>> acknowledged = new ArrayList<>();

    private RecordMatchEventUseCase recording = (event, topic) -> {
        calls.add("record");
        topics.add(topic);
        return new RecordResult.Accepted(event.id());
    };

    private final AcknowledgeEventUseCase acknowledging = (venue, results) -> {
        calls.add("acknowledge");
        acknowledgedVenues.add(venue);
        acknowledged.add(results);
    };

    private MatchEventListener listener() {
        return new MatchEventListener(recording, acknowledging);
    }

    @Test
    void recordsAndThenAcknowledgesInThisOrder() {
        listener().onMessage(TOPIC, GOAL);

        assertThat(calls).containsExactly("record", "acknowledge");
        assertThat(acknowledged.get(0)).containsExactly(new RecordResult.Accepted(new EventId(EVENT)));
    }

    @Test
    void passesTheCoordinatesOfTheTopicToTheUseCase() {
        listener().onMessage(TOPIC, GOAL);

        assertThat(topics.get(0).venueId().value()).isEqualTo(VENUE);
        assertThat(topics.get(0).tableId().value()).isEqualTo(TABLE);
        assertThat(topics.get(0).matchId().value()).isEqualTo(MATCH);
    }

    @Test
    void theAckGoesToTheVenueOfTheTopicNotToTheOneOfTheEvent() {
        UUID other = UUID.randomUUID();
        String claiming = GOAL.replace(VENUE.toString(), other.toString());

        listener().onMessage(TOPIC, claiming);

        assertThat(acknowledgedVenues).containsExactly(new VenueId(VENUE));
    }

    @Test
    void anUnreadableEventWithIdIsAnsweredWithARejection() {
        listener().onMessage(TOPIC, envelope("GoalScored", 2, "{\"side\":\"GREEN\"}"));

        assertThat(calls).containsExactly("acknowledge");
        assertThat(acknowledged.get(0)).singleElement().isInstanceOfSatisfying(RecordResult.Rejected.class,
                r -> assertThat(r.eventId()).isEqualTo(new EventId(EVENT)));
    }

    @Test
    void anUnreadableEventWithoutIdIsDropped() {
        listener().onMessage(TOPIC, "not json");

        assertThat(calls).isEmpty();
    }

    @Test
    void aMessageOnAnotherTopicIsDropped() {
        listener().onMessage("venues/" + VENUE + "/hub/ack", GOAL);
        listener().onMessage("venues/not-a-uuid/tables/" + TABLE + "/matches/" + MATCH + "/events", GOAL);

        assertThat(calls).isEmpty();
    }

    @Test
    void ifSavingFailsThereIsNoAckAndTheErrorGoesUp() {
        recording = (MatchEvent event, EventTopic topic) -> {
            throw new IllegalStateException("database down");
        };

        assertThatThrownBy(() -> listener().onMessage(TOPIC, GOAL)).isInstanceOf(IllegalStateException.class);
        assertThat(calls).isEmpty();
    }
}
