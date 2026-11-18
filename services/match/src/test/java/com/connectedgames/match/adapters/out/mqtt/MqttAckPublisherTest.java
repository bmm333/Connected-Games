package com.connectedgames.match.adapters.out.mqtt;

import com.connectedgames.match.application.port.out.Ack;
import com.connectedgames.match.application.port.out.Rejection;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.VenueId;
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

class MqttAckPublisherTest {

    private static final UUID VENUE = UUID.randomUUID();
    private static final EventId SAVED = new EventId(UUID.randomUUID());
    private static final EventId REFUSED = new EventId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-16T20:00:00Z");

    private final List<Message<?>> sent = new ArrayList<>();
    private final MessageChannel channel = (message, timeout) -> sent.add(message);

    private Ack ack() {
        return new Ack(new VenueId(VENUE), List.of(SAVED), List.of(new Rejection(REFUSED, "seq already used")), NOW);
    }

    @Test
    void publishesOnTheAckTopicOfTheVenueWithQos1AndNotRetained() {
        new MqttAckPublisher(channel).publish(ack());

        Message<?> message = sent.get(0);
        assertThat(message.getHeaders().get(MqttHeaders.TOPIC)).isEqualTo("venues/" + VENUE + "/hub/ack");
        assertThat(message.getHeaders().get(MqttHeaders.QOS)).isEqualTo(1);
        assertThat(message.getHeaders().get(MqttHeaders.RETAINED)).isEqualTo(false);
    }

    @Test
    void theBodyFollowsTheContract() {
        new MqttAckPublisher(channel).publish(ack());

        JsonNode body = JsonMapper.builder().build().readTree((String) sent.get(0).getPayload());
        assertThat(body.get("venueId").asString()).isEqualTo(VENUE.toString());
        assertThat(body.get("acknowledged")).hasSize(1);
        assertThat(body.get("acknowledged").get(0).asString()).isEqualTo(SAVED.value().toString());
        assertThat(body.get("rejected").get(0).get("eventId").asString()).isEqualTo(REFUSED.value().toString());
        assertThat(body.get("rejected").get(0).get("reason").asString()).isEqualTo("seq already used");
        assertThat(body.get("processedAt").asString()).isEqualTo("2026-11-16T20:00:00Z");
    }

    @Test
    void ifTheChannelRefusesTheErrorGoesUpSoThereIsNoSilentLoss() {
        MessageChannel refusing = (message, timeout) -> false;

        assertThatThrownBy(() -> new MqttAckPublisher(refusing).publish(ack()))
                .isInstanceOf(IllegalStateException.class);
    }
}
