package com.connectedgames.match.adapters.in.mqtt;

import com.connectedgames.match.application.port.in.AcknowledgeEventUseCase;
import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.MatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class MatchEventListener {

    private static final Logger log = LoggerFactory.getLogger(MatchEventListener.class);

    private final RecordMatchEventUseCase record;
    private final AcknowledgeEventUseCase acknowledge;
    private final MatchEventParser parser = new MatchEventParser();

    public MatchEventListener(RecordMatchEventUseCase record, AcknowledgeEventUseCase acknowledge) {
        this.record = record;
        this.acknowledge = acknowledge;
    }

    public void onMessage(String topic, String payload) {
        EventTopic eventTopic;
        try {
            eventTopic = topicOf(topic);
        } catch (IllegalArgumentException e) {
            // Nobody to answer
            log.warn("Message on a topic that is not a match topic, dropped: {}", topic);
            return;
        }
        try {
            MatchEvent event = parser.parse(payload);
            RecordResult result = record.record(event, eventTopic);
            // The ack goes to the venue of the TOPIC, the one the broker authorized
            acknowledge.acknowledge(eventTopic.venueId(), List.of(result));
        } catch (UnreadableMessageException e) {
            if (e.eventId() == null) {
                log.warn("Unreadable message on {}, dropped: {}", topic, e.getMessage());
                return;
            }
            acknowledge.acknowledge(eventTopic.venueId(), List.of(new RecordResult.Rejected(e.eventId(), e.getMessage())));
        }
        // Any other exception is not caught no ack, so the hub sends the event again
    }

    static EventTopic topicOf(String topic) {
        String[] p = topic.split("/");
        if (p.length != 7 || !p[0].equals("venues") || !p[2].equals("tables")
                || !p[4].equals("matches") || !p[6].equals("events")) {
            throw new IllegalArgumentException("Not a match topic: " + topic);
        }
        return new EventTopic(new VenueId(UUID.fromString(p[1])), new TableId(UUID.fromString(p[3])),
                new MatchId(UUID.fromString(p[5])));
    }
}
