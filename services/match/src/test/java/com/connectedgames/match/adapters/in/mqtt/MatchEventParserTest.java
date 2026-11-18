package com.connectedgames.match.adapters.in.mqtt;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.event.MatchStarted;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchEventParserTest {

    static final UUID EVENT = UUID.randomUUID();
    static final UUID VENUE = UUID.randomUUID();
    static final UUID TABLE = UUID.randomUUID();
    static final UUID MATCH = UUID.randomUUID();

    private final MatchEventParser parser = new MatchEventParser();

    static String envelope(String type, int seq, String payload) {
        return """
                {"eventId":"%s","tenantId":"acme","venueId":"%s","tableId":"%s","matchId":"%s",
                 "seq":%d,"type":"%s","occurredAt":"2026-11-16T20:00:00Z","source":"CAMERA","payload":%s}"""
                .formatted(EVENT, VENUE, TABLE, MATCH, seq, type, payload);
    }

    @Test
    void readsAMatchStarted() {
        MatchEvent event = parser.parse(envelope("MatchStarted", 1, """
                {"mode":"INDIVIDUAL","pointsToWin":5,"sides":[
                  {"side":"RED","participantId":"p1","displayName":"Ann"},{"side":"BLUE"}]}"""));

        MatchStarted started = (MatchStarted) event;
        assertThat(started.id()).isEqualTo(new EventId(EVENT));
        assertThat(started.seq()).isEqualTo(1);
        assertThat(started.occurredAt()).isEqualTo(Instant.parse("2026-11-16T20:00:00Z"));
        assertThat(started.mode()).isEqualTo(GameMode.INDIVIDUAL);
        assertThat(started.pointsToWin()).isEqualTo(5);
        assertThat(started.sides()).hasSize(2);
        assertThat(started.sides().get(0).participantId()).isEqualTo("p1");
        assertThat(started.sides().get(1).participantId()).isNull();
    }

    @Test
    void readsAGoal() {
        GoalScored goal = (GoalScored) parser.parse(envelope("GoalScored", 2, "{\"side\":\"BLUE\"}"));

        assertThat(goal.side()).isEqualTo(Side.BLUE);
        assertThat(goal.venueId().value()).isEqualTo(VENUE);
        assertThat(goal.tableId().value()).isEqualTo(TABLE);
        assertThat(goal.matchId().value()).isEqualTo(MATCH);
    }

    @Test
    void readsAMatchEndedWithoutWinner() {
        MatchEnded ended = (MatchEnded) parser.parse(envelope("MatchEnded", 9, """
                {"reason":"TIME_EXPIRED","finalScore":[{"side":"RED","score":2},{"side":"BLUE","score":2}]}"""));

        assertThat(ended.reason()).isEqualTo(EndReason.TIME_EXPIRED);
        assertThat(ended.finalScore()).hasSize(2);
        assertThat(ended.winnerSide()).isNull();
    }

    @Test
    void notJsonIsUnreadableAndHasNoId() {
        assertThatThrownBy(() -> parser.parse("not json"))
                .isInstanceOfSatisfying(UnreadableMessageException.class, e -> assertThat(e.eventId()).isNull());
    }

    @Test
    void anUnknownTypeIsUnreadableButKeepsTheId() {
        assertThatThrownBy(() -> parser.parse(envelope("Goool", 2, "{}")))
                .isInstanceOfSatisfying(UnreadableMessageException.class, e -> {
                    assertThat(e.eventId()).isEqualTo(new EventId(EVENT));
                    assertThat(e.getMessage()).contains("Goool");
                });
    }

    @Test
    void aSideThatDoesNotExistIsUnreadableButKeepsTheId() {
        assertThatThrownBy(() -> parser.parse(envelope("GoalScored", 2, "{\"side\":\"GREEN\"}")))
                .isInstanceOfSatisfying(UnreadableMessageException.class,
                        e -> assertThat(e.eventId()).isEqualTo(new EventId(EVENT)));
    }

    @Test
    void aMissingSideIsUnreadable() {
        assertThatThrownBy(() -> parser.parse(envelope("GoalScored", 2, "{}")))
                .isInstanceOf(UnreadableMessageException.class);
    }

    @Test
    void aSeqUnderOneIsUnreadable() {
        assertThatThrownBy(() -> parser.parse(envelope("GoalScored", 0, "{\"side\":\"RED\"}")))
                .isInstanceOf(UnreadableMessageException.class);
    }

    @Test
    void aMissingMatchIdIsUnreadable() {
        String json = envelope("GoalScored", 2, "{\"side\":\"RED\"}").replace("\"matchId\":\"" + MATCH + "\",", "");

        assertThatThrownBy(() -> parser.parse(json)).isInstanceOf(UnreadableMessageException.class);
    }

    @Test
    void aFieldWeDoNotKnowIsIgnoredSoANewerHubIsNotRejectedForever() {
        GoalScored goal = (GoalScored) parser.parse(envelope("GoalScored", 2, "{\"side\":\"RED\",\"speed\":12}"));

        assertThat(goal.side()).isEqualTo(Side.RED);
    }
}
