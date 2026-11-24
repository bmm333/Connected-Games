package com.connectedgames.match.adapters.out.mqtt;

import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedPublisher;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.event.EndReason;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

/**
 * Tells stats and tournament that a match is closed: platform/matches/completed, as in
 * contracts/schemas/match-completed.schema.json. Fields that are missing are left out, the schema does not accept null.
 */
public class MqttMatchCompletedPublisher implements MatchCompletedPublisher {

    static final String TOPIC = "platform/matches/completed";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record SideMessage(Side side, String participantId, String displayName, int score) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record PointMessage(int seq, Side side, Instant occurredAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record CompletedMessage(String eventId, String tenantId, String venueId, String tableId, String matchId,
                                    String gameTypeId, String tournamentMatchId, GameMode mode, int pointsToWin,
                                    Instant startedAt, Instant endedAt, EndReason reason, Side winnerSide,
                                    List<SideMessage> sides, List<PointMessage> points) {}

    private final MessageChannel channel;

    public MqttMatchCompletedPublisher(MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void publish(MatchCompleted m) {
        CompletedMessage body = new CompletedMessage(m.eventId().value().toString(), m.tenantId().value(),
                m.venueId().value().toString(), m.tableId().value().toString(), m.matchId().value().toString(),
                m.gameTypeId().value().toString(),
                m.tournamentMatchId() == null ? null : m.tournamentMatchId().value().toString(),
                m.mode(), m.pointsToWin(), m.startedAt(), m.endedAt(), m.reason(), m.winnerSide(),
                m.sides().stream().map(s -> new SideMessage(s.side(), s.participantId(), s.displayName(), s.score())).toList(),
                m.points().stream().map(x -> new PointMessage(x.seq(), x.side(), x.occurredAt())).toList());
        // The handler is not async: when send returns without an exception the broker has answered the QoS 1 publish.
        boolean sent = channel.send(MessageBuilder.withPayload(JSON.writeValueAsString(body))
                .setHeader(MqttHeaders.TOPIC, TOPIC)
                .setHeader(MqttHeaders.QOS, 1)
                .setHeader(MqttHeaders.RETAINED, false)
                .build());
        if (!sent) {
            throw new IllegalStateException("The MatchCompleted of match " + m.matchId().value() + " was not sent");
        }
    }
}
