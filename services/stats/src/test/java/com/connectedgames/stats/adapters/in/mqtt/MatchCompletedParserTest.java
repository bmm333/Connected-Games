package com.connectedgames.stats.adapters.in.mqtt;

import com.connectedgames.stats.Messages;
import com.connectedgames.stats.domain.EndReason;
import com.connectedgames.stats.domain.GameMode;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.result.MatchResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchCompletedParserTest {

    /** What the match service really publishes: copied from the output of its MqttMatchCompletedPublisher. */
    private static final String FROM_THE_MATCH_SERVICE =
            "{\"eventId\":\"fbc89976-5ffd-4344-93c6-86a5ece7103e\",\"tenantId\":\"00000000-0000-4000-8000-0000000000a1\",\"venueId\":\"b6c768ed-8eba-474f-b1c5-4348f4b11eeb\",\"tableId\":\"68a9a72d-2d75-42c6-b465-d3223d558949\",\"matchId\":\"191da781-4219-4e23-aa4d-7215e06e4893\",\"gameTypeId\":\"00000000-0000-4000-8000-0000000000b1\",\"tournamentMatchId\":\"00000000-0000-4000-8000-0000000000c1\",\"mode\":\"TEAM\",\"pointsToWin\":5,\"startedAt\":\"2026-11-16T20:00:00Z\",\"endedAt\":\"2026-11-16T20:10:00Z\",\"reason\":\"SCORE_REACHED\",\"winnerSide\":\"RED\",\"sides\":[{\"side\":\"RED\",\"participantId\":\"team-1\",\"displayName\":\"Lightning\",\"score\":5},{\"side\":\"BLUE\",\"displayName\":\"Blue\",\"score\":3}],\"points\":[{\"seq\":2,\"side\":\"RED\",\"occurredAt\":\"2026-11-16T20:00:30Z\"},{\"seq\":3,\"side\":\"RED\",\"occurredAt\":\"2026-11-16T20:01:00Z\"},{\"seq\":4,\"side\":\"BLUE\",\"occurredAt\":\"2026-11-16T20:01:30Z\"},{\"seq\":5,\"side\":\"RED\",\"occurredAt\":\"2026-11-16T20:02:00Z\"},{\"seq\":6,\"side\":\"BLUE\",\"occurredAt\":\"2026-11-16T20:02:30Z\"},{\"seq\":7,\"side\":\"RED\",\"occurredAt\":\"2026-11-16T20:03:00Z\"},{\"seq\":8,\"side\":\"BLUE\",\"occurredAt\":\"2026-11-16T20:03:30Z\"},{\"seq\":9,\"side\":\"RED\",\"occurredAt\":\"2026-11-16T20:04:00Z\"}]}";

    private final MatchCompletedParser parser = new MatchCompletedParser();

    private static void assertUnreadable(MatchCompletedParser parser, String json) {
        assertThatThrownBy(() -> parser.parse(json)).isInstanceOf(UnreadableMessageException.class);
    }

    @Test
    void readsWhatTheMatchServiceReallyPublishes() {
        MatchResult result = parser.parse(FROM_THE_MATCH_SERVICE);

        assertThat(result.id().value()).isEqualTo(UUID.fromString("191da781-4219-4e23-aa4d-7215e06e4893"));
        assertThat(result.messageId().value()).isEqualTo(UUID.fromString("fbc89976-5ffd-4344-93c6-86a5ece7103e"));
        assertThat(result.tenantId().value()).isEqualTo("00000000-0000-4000-8000-0000000000a1");
        assertThat(result.tournamentMatchId().value()).isEqualTo(UUID.fromString("00000000-0000-4000-8000-0000000000c1"));
        assertThat(result.mode()).isEqualTo(GameMode.TEAM);
        assertThat(result.pointsToWin()).isEqualTo(5);
        assertThat(result.reason()).isEqualTo(EndReason.SCORE_REACHED);
        assertThat(result.winnerSide()).isEqualTo(Side.RED);
        assertThat(result.startedAt()).isEqualTo(Instant.parse("2026-11-16T20:00:00Z"));
        assertThat(result.endedAt()).isEqualTo(Instant.parse("2026-11-16T20:10:00Z"));
        assertThat(result.sides()).hasSize(2);
        assertThat(result.sides().get(0).participantId()).isEqualTo("team-1");
        assertThat(result.sides().get(0).displayName()).isEqualTo("Lightning");
        assertThat(result.sides().get(0).score()).isEqualTo(5);
        assertThat(result.sides().get(1).participantId()).isNull();
        assertThat(result.sides().get(1).score()).isEqualTo(3);
        assertThat(result.points()).hasSize(8);
        assertThat(result.points().get(0).seq()).isEqualTo(2);
        assertThat(result.points().get(0).side()).isEqualTo(Side.RED);
        assertThat(result.points().get(0).occurredAt()).isEqualTo(Instant.parse("2026-11-16T20:00:30Z"));
        assertThat(result.points().get(2).side()).isEqualTo(Side.BLUE);
    }

    @Test
    void readsAMessageWithOnlyTheRequiredFields() {
        MatchResult result = parser.parse(Messages.draw(UUID.randomUUID()));

        assertThat(result.tournamentMatchId()).isNull();
        assertThat(result.winnerSide()).isNull();
        assertThat(result.sides().get(0).participantId()).isNull();
        assertThat(result.sides().get(0).displayName()).isNull();
        assertThat(result.points()).isEmpty();
        assertThat(result.reason()).isEqualTo(EndReason.TIME_EXPIRED);
    }

    @Test
    void aMessageWithoutThePointsListIsReadAsNoPoints() {
        String withoutPoints = Messages.draw(UUID.randomUUID()).replace(",\"points\":[]", "");

        assertThat(parser.parse(withoutPoints).points()).isEmpty();
    }

    @Test
    void fieldsItDoesNotKnowAreIgnoredSoTheMatchServiceCanAddSome() {
        String json = Messages.completed(UUID.randomUUID())
                .replace("\"mode\":", "\"source\":\"CAMERA\",\"mode\":")
                .replace("\"score\":5", "\"score\":5,\"rank\":1")
                .replace("\"seq\":2,", "\"seq\":2,\"scorer\":\"p1\",");

        assertThat(parser.parse(json).points()).hasSize(3);
    }

    @Test
    void timesWithNanosecondsAreRead() {
        String json = Messages.draw(UUID.randomUUID()).replace("2026-11-16T20:00:00Z", "2026-11-16T20:00:00.123456789Z");

        assertThat(parser.parse(json).startedAt()).isEqualTo(Instant.parse("2026-11-16T20:00:00.123456789Z"));
    }

    @Test
    void aCancelledMatchIsReadToo() {
        assertThat(parser.parse(Messages.cancelled(UUID.randomUUID())).reason()).isEqualTo(EndReason.CANCELLED);
    }

    @Test
    void whatIsNotJsonIsUnreadable() {
        assertUnreadable(parser, "not json");
        assertUnreadable(parser, "");
        assertUnreadable(parser, "[1,2,3]");
        assertUnreadable(parser, "\"text\"");
    }

    @Test
    void aMessageWithAnUnknownEnumValueIsUnreadable() {
        assertUnreadable(parser, Messages.completed(UUID.randomUUID()).replace("SCORE_REACHED", "GOLDEN_GOAL"));
        assertUnreadable(parser, Messages.completed(UUID.randomUUID()).replace("\"TEAM\"", "\"TRIO\""));
        assertUnreadable(parser, Messages.completed(UUID.randomUUID()).replace("\"winnerSide\":\"RED\"", "\"winnerSide\":\"GREEN\""));
    }

    @Test
    void aMessageMissingARequiredFieldIsUnreadable() {
        for (String field : new String[]{"\"tenantId\":\"acme\",", "\"gameTypeId\":\"00000000-0000-4000-8000-0000000000b1\",",
                "\"mode\":\"INDIVIDUAL\",", "\"reason\":\"TIME_EXPIRED\","}) {
            assertUnreadable(parser, Messages.draw(UUID.randomUUID()).replace(field, ""));
        }
        assertUnreadable(parser, Messages.draw(UUID.randomUUID()).replace("\"matchId\"", "\"match\""));
        assertUnreadable(parser, Messages.draw(UUID.randomUUID()).replaceAll("\"sides\":\\[.*\\],\"points\"", "\"points\""));
    }

    @Test
    void aMessageThatBreaksTheRulesOfAMatchIsUnreadable() {
        String draw = Messages.draw(UUID.randomUUID());
        assertUnreadable(parser, draw.replace("\"pointsToWin\":5", "\"pointsToWin\":0"));
        assertUnreadable(parser, draw.replace("\"score\":2},{\"side\":\"BLUE\"", "\"score\":-2},{\"side\":\"BLUE\""));
        assertUnreadable(parser, draw.replace("{\"side\":\"BLUE\",\"score\":2}", "{\"side\":\"RED\",\"score\":2}"));
        assertUnreadable(parser, draw.replace("\"endedAt\":\"2026-11-16T20:10:00Z\"", "\"endedAt\":\"2026-11-16T19:00:00Z\""));
        assertUnreadable(parser, draw.replace("\"tenantId\":\"acme\"", "\"tenantId\":\" \""));
        assertUnreadable(parser, draw.replace("\"side\":\"RED\",\"score\":2", "\"side\":\"RED\",\"participantId\":\"\",\"score\":2"));
    }

    @Test
    void aBadIdOrTimeIsUnreadable() {
        assertUnreadable(parser, Messages.draw(UUID.randomUUID()).replace("\"venueId\":\"", "\"venueId\":\"x"));
        assertUnreadable(parser, Messages.draw(UUID.randomUUID()).replace("2026-11-16T20:00:00Z", "yesterday"));
    }

    @Test
    void pointsWithABadSeqOrOutOfOrderAreUnreadable() {
        String full = Messages.completed(UUID.randomUUID());
        assertUnreadable(parser, full.replace("\"seq\":2", "\"seq\":0"));
        assertUnreadable(parser, full.replace("\"seq\":3", "\"seq\":2"));
        assertUnreadable(parser, full.replace("\"seq\":3", "\"seq\":1"));
    }
}
