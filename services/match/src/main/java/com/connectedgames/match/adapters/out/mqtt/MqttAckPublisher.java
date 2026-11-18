package com.connectedgames.match.adapters.out.mqtt;

import com.connectedgames.match.application.port.out.Ack;
import com.connectedgames.match.application.port.out.AckPublisher;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

/**
 * Tells hub which events are saved: venues/{venueId}/hub/ack, as in contracts/schemas/events-acknowledged.schema.json.
 */
public class MqttAckPublisher implements AckPublisher {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private record AckMessage(String venueId, List<String> acknowledged, List<RejectedMessage> rejected, Instant processedAt) {}
    private record RejectedMessage(String eventId, String reason) {}

    private final MessageChannel channel;

    public MqttAckPublisher(MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void publish(Ack ack) {
        String venueId = ack.venueId().value().toString();
        AckMessage body = new AckMessage(venueId,
                ack.acknowledged().stream().map(id -> id.value().toString()).toList(),
                ack.rejected().stream().map(r -> new RejectedMessage(r.eventId().value().toString(), r.reason())).toList(),
                ack.processedAt());
        boolean sent = channel.send(MessageBuilder.withPayload(JSON.writeValueAsString(body))
                .setHeader(MqttHeaders.TOPIC, "venues/" + venueId + "/hub/ack")
                .setHeader(MqttHeaders.QOS, 1)
                .setHeader(MqttHeaders.RETAINED, false)
                .build());
        if (!sent) {
            throw new IllegalStateException("The ack for venue " + venueId + " was not sent");
        }
    }
}
