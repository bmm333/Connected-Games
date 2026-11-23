package com.connectedgames.match.adapters.out.mqtt;

import com.connectedgames.match.Fixtures;
import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.match.MatchSide;
import org.junit.jupiter.api.Test;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MqttMatchCompletedPublisherTest {

    private final List<Message<?>> sent = new ArrayList<>();
    private final MessageChannel channel = (message, timeout) -> sent.add(message);

    private JsonNode bodyOf(MatchCompleted message) {
        new MqttMatchCompletedPublisher(channel).publish(message);
        return JsonMapper.builder().build().readTree((String) sent.get(0).getPayload());
    }

    @Test
    void publishesOnThePlatformTopicWithQos1AndNotRetained() {
        new MqttMatchCompletedPublisher(channel).publish(Fixtures.completed());

        Message<?> message = sent.get(0);
        assertThat(message.getHeaders().get(MqttHeaders.TOPIC)).isEqualTo("platform/matches/completed");
        assertThat(message.getHeaders().get(MqttHeaders.QOS)).isEqualTo(1);
        assertThat(message.getHeaders().get(MqttHeaders.RETAINED)).isEqualTo(false);
    }

    @Test
    void theBodyHasExactlyTheFieldsOfTheContract() {
        MatchCompleted message = Fixtures.completed();

        JsonNode body = bodyOf(message);

        assertThat(body.propertyNames()).containsExactlyInAnyOrder("eventId", "tenantId", "venueId", "tableId",
                "matchId", "gameTypeId", "tournamentMatchId", "mode", "pointsToWin", "startedAt", "endedAt",
                "reason", "winnerSide", "sides", "points");
        assertThat(body.get("eventId").asString()).isEqualTo(message.eventId().value().toString());
        assertThat(body.get("tenantId").asString()).isEqualTo(Fixtures.TENANT.value());
        assertThat(body.get("matchId").asString()).isEqualTo(message.matchId().value().toString());
        assertThat(body.get("gameTypeId").asString()).isEqualTo(Fixtures.GAME_TYPE.value().toString());
        assertThat(body.get("tournamentMatchId").asString()).isEqualTo(Fixtures.TOURNAMENT_MATCH.value().toString());
        assertThat(body.get("mode").asString()).isEqualTo("TEAM");
        assertThat(body.get("pointsToWin").asInt()).isEqualTo(5);
        assertThat(body.get("startedAt").asString()).isEqualTo("2026-11-16T20:00:00Z");
        assertThat(body.get("endedAt").asString()).isEqualTo("2026-11-16T20:10:00Z");
        assertThat(body.get("reason").asString()).isEqualTo("SCORE_REACHED");
        assertThat(body.get("winnerSide").asString()).isEqualTo("RED");
        assertThat(body.get("sides")).hasSize(2);
        assertThat(body.get("sides").get(0).get("side").asString()).isEqualTo("RED");
        assertThat(body.get("sides").get(0).get("participantId").asString()).isEqualTo("team-1");
        assertThat(body.get("sides").get(0).get("displayName").asString()).isEqualTo("Lightning");
        assertThat(body.get("sides").get(0).get("score").asInt()).isEqualTo(5);
    }

    @Test
    void thePointsAreListedInOrderWithSeqSideAndTime() {
        JsonNode body = bodyOf(Fixtures.completed());

        assertThat(body.get("points")).hasSize(8);
        JsonNode first = body.get("points").get(0);
        assertThat(first.propertyNames()).containsExactlyInAnyOrder("seq", "side", "occurredAt");
        assertThat(first.get("seq").asInt()).isEqualTo(2);
        assertThat(first.get("side").asString()).isEqualTo("RED");
        assertThat(first.get("occurredAt").asString()).isEqualTo("2026-11-16T20:00:30Z");
        assertThat(body.get("points").get(2).get("side").asString()).isEqualTo("BLUE");
        assertThat(body.get("points").get(7).get("seq").asInt()).isEqualTo(9);
    }

    @Test
    void whatIsMissingIsLeftOutBecauseTheSchemaDoesNotAcceptNull() {
        MatchCompleted draw = new MatchCompleted(new EventId(UUID.randomUUID()), Fixtures.TENANT,
                Fixtures.completed().venueId(), Fixtures.completed().tableId(), new MatchId(UUID.randomUUID()),
                Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, Instant.parse("2026-11-16T20:00:00Z"),
                Instant.parse("2026-11-16T20:10:00Z"), EndReason.TIME_EXPIRED, null,
                List.of(new MatchSide(Side.RED, null, null, 2), new MatchSide(Side.BLUE, null, "Blue", 2)), List.of());

        JsonNode body = bodyOf(draw);

        assertThat(body.has("tournamentMatchId")).isFalse();
        assertThat(body.has("winnerSide")).isFalse();
        // points is required by the schema: with no goals it is an empty list, not missing
        assertThat(body.has("points")).isTrue();
        assertThat(body.get("points")).isEmpty();
        assertThat(body.get("sides").get(0).has("participantId")).isFalse();
        assertThat(body.get("sides").get(0).has("displayName")).isFalse();
        assertThat(body.get("sides").get(1).get("displayName").asString()).isEqualTo("Blue");
    }

    @Test
    void ifTheChannelRefusesTheErrorGoesUpSoTheMessageStaysInTheOutbox() {
        MessageChannel refusing = (message, timeout) -> false;

        assertThatThrownBy(() -> new MqttMatchCompletedPublisher(refusing).publish(Fixtures.completed()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ifTheBrokerFailsTheErrorGoesUp() {
        MessageChannel failing = (message, timeout) -> {
            throw new IllegalStateException("broker down");
        };

        assertThatThrownBy(() -> new MqttMatchCompletedPublisher(failing).publish(Fixtures.completed()))
                .isInstanceOf(IllegalStateException.class);
    }
}
