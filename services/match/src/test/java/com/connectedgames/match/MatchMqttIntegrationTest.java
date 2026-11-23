package com.connectedgames.match;

import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchStatus;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Real broker, real database: a hub publishes, the service saves and answers on the ack topic. */
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
@SpringBootTest
class MatchMqttIntegrationTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    MatchRepository matches;

    @Autowired
    MatchCompletedOutbox outbox;

    @Value("${match.mqtt.url}")
    String brokerUrl;

    // new ids for each test: the database is shared by the tests of the class
    private final UUID venue = UUID.randomUUID();
    private final UUID table = UUID.randomUUID();
    private final UUID match = UUID.randomUUID();
    private final BlockingQueue<JsonNode> acks = new LinkedBlockingQueue<>();
    private final BlockingQueue<JsonNode> completed = new LinkedBlockingQueue<>();
    private MqttClient hub;

    @BeforeEach
    void connectTheHub() throws Exception {
        hub = new MqttClient(brokerUrl, "test-hub-" + UUID.randomUUID(), null);
        hub.connect();
        hub.subscribe("venues/" + venue + "/hub/ack", 1,
                (topic, message) -> acks.add(JSON.readTree(new String(message.getPayload(), StandardCharsets.UTF_8))));
    }

    @AfterEach
    void disconnectTheHub() throws Exception {
        hub.disconnect();
        hub.close();
    }

    private String topic() {
        return "venues/%s/tables/%s/matches/%s/events".formatted(venue, table, match);
    }

    private String event(UUID eventId, int seq, String type, String payload) {
        return """
                {"eventId":"%s","tenantId":"acme","venueId":"%s","tableId":"%s","matchId":"%s",
                 "seq":%d,"type":"%s","occurredAt":"2026-11-16T20:00:00Z","source":"CAMERA","payload":%s}"""
                .formatted(eventId, venue, table, match, seq, type, payload);
    }

    private UUID publishStart(int seq) throws Exception {
        UUID id = UUID.randomUUID();
        publish(topic(), event(id, seq, "MatchStarted",
                "{\"gameTypeId\":\"00000000-0000-4000-8000-0000000000b1\",\"mode\":\"INDIVIDUAL\",\"pointsToWin\":5,\"sides\":[{\"side\":\"RED\"},{\"side\":\"BLUE\"}]}"));
        return id;
    }

    private UUID publishGoal(int seq, String side) throws Exception {
        UUID id = UUID.randomUUID();
        publishGoal(id, seq, side);
        return id;
    }

    private void publishGoal(UUID id, int seq, String side) throws Exception {
        publish(topic(), event(id, seq, "GoalScored", "{\"side\":\"" + side + "\"}"));
    }

    private void publish(String topic, String json) throws Exception {
        hub.publish(topic, json.getBytes(StandardCharsets.UTF_8), 1, false);
    }

    private JsonNode nextAck() throws Exception {
        JsonNode ack = acks.poll(10, TimeUnit.SECONDS);
        assertThat(ack).as("an ack on the topic of the venue within 10 seconds").isNotNull();
        return ack;
    }

    private UUID publishEnd(int seq) throws Exception {
        UUID id = UUID.randomUUID();
        publish(topic(), event(id, seq, "MatchEnded",
                "{\"reason\":\"SCORE_REACHED\",\"finalScore\":[{\"side\":\"RED\",\"score\":5},{\"side\":\"BLUE\",\"score\":3}],\"winnerSide\":\"RED\"}"));
        return id;
    }

    /** The MatchCompleted of this test's match: the topic is shared, so messages of other matches are skipped. */
    private JsonNode nextCompleted() throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            JsonNode message = completed.poll(1, TimeUnit.SECONDS);
            if (message != null && message.get("matchId").asString().equals(match.toString())) {
                return message;
            }
        }
        throw new AssertionError("no MatchCompleted of the match within 10 seconds");
    }

    private Match savedMatch() {
        return matches.findById(new MatchId(match)).orElseThrow();
    }

    @Test
    void aMatchStartedIsSavedAndAcknowledged() throws Exception {
        UUID started = publishStart(1);

        JsonNode ack = nextAck();

        assertThat(ack.get("venueId").asString()).isEqualTo(venue.toString());
        assertThat(ack.get("acknowledged").get(0).asString()).isEqualTo(started.toString());
        assertThat(ack.get("rejected")).isEmpty();
        assertThat(savedMatch().status()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    void theTenantAndTheGameTypeOfTheHubAreKept() throws Exception {
        publishStart(1);
        nextAck();

        assertThat(savedMatch().tenantId().value()).isEqualTo("acme");
        assertThat(savedMatch().gameTypeId().value().toString()).isEqualTo("00000000-0000-4000-8000-0000000000b1");
        assertThat(savedMatch().tournamentMatchId()).isNull();
    }

    @Test
    void aClosedMatchIsPublishedForStatsAndTournament() throws Exception {
        hub.subscribe("platform/matches/completed", 1, (topic, message) ->
                completed.add(JSON.readTree(new String(message.getPayload(), StandardCharsets.UTF_8))));
        publishStart(1);
        nextAck();
        publishGoal(2, "RED");
        nextAck();
        assertThat(completed).as("nothing while the match is in progress").isEmpty();

        publishEnd(3);
        nextAck();
        JsonNode message = nextCompleted();

        assertThat(message.get("tenantId").asString()).isEqualTo("acme");
        assertThat(message.get("venueId").asString()).isEqualTo(venue.toString());
        assertThat(message.get("tableId").asString()).isEqualTo(table.toString());
        assertThat(message.get("gameTypeId").asString()).isEqualTo("00000000-0000-4000-8000-0000000000b1");
        assertThat(message.has("tournamentMatchId")).isFalse();
        assertThat(message.get("reason").asString()).isEqualTo("SCORE_REACHED");
        assertThat(message.get("winnerSide").asString()).isEqualTo("RED");
        assertThat(message.get("sides").get(0).get("score").asInt()).isEqualTo(5);
        assertThat(message.get("sides").get(1).get("score").asInt()).isEqualTo(3);
    }

    @Test
    void theMessageIsMarkedPublishedOnceTheBrokerHasIt() throws Exception {
        hub.subscribe("platform/matches/completed", 1, (topic, message) ->
                completed.add(JSON.readTree(new String(message.getPayload(), StandardCharsets.UTF_8))));
        publishStart(1);
        nextAck();
        publishEnd(2);
        nextAck();
        UUID eventId = UUID.fromString(nextCompleted().get("eventId").asString());

        // the mark comes right after the broker answered: give it a moment, then it must be there
        long deadline = System.currentTimeMillis() + 5_000;
        while (stillWaiting(eventId) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }

        assertThat(stillWaiting(eventId)).isFalse();
    }

    private boolean stillWaiting(UUID eventId) {
        return outbox.unpublished(1000).stream().map(MatchCompleted::eventId)
                .anyMatch(id -> id.value().equals(eventId));
    }

    @Test
    void aMatchEndedTwiceIsPublishedOnce() throws Exception {
        hub.subscribe("platform/matches/completed", 1, (topic, message) ->
                completed.add(JSON.readTree(new String(message.getPayload(), StandardCharsets.UTF_8))));
        publishStart(1);
        nextAck();
        publishEnd(2);
        nextAck();
        nextCompleted();

        publishEnd(3);
        nextAck();
        Thread.sleep(2_500);    // more than two passes of the publisher

        long ofThisMatch = completed.stream().filter(m -> m.get("matchId").asString().equals(match.toString())).count();
        assertThat(ofThisMatch).isZero();
    }

    @Test
    void theAckComesAfterTheDataIsInTheDatabase() throws Exception {
        publishStart(1);
        nextAck();
        publishGoal(2, "RED");

        nextAck();

        // no waiting after the ack: if the ack came first, this could still be 0
        assertThat(savedMatch().scoreOf(Side.RED)).isEqualTo(1);
    }

    @Test
    void aDuplicateIsAcknowledgedAgainButCountedOnce() throws Exception {
        publishStart(1);
        nextAck();
        UUID goal = publishGoal(2, "RED");
        nextAck();

        publishGoal(goal, 2, "RED");
        JsonNode again = nextAck();

        assertThat(again.get("acknowledged").get(0).asString()).isEqualTo(goal.toString());
        assertThat(savedMatch().scoreOf(Side.RED)).isEqualTo(1);
    }

    @Test
    void anEventThatClaimsAnotherMatchThanItsTopicIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        String claimingAnother = event(id, 1, "GoalScored", "{\"side\":\"RED\"}").replace(match.toString(), UUID.randomUUID().toString());

        publish(topic(), claimingAnother);
        JsonNode ack = nextAck();

        assertThat(ack.get("acknowledged")).isEmpty();
        assertThat(ack.get("rejected").get(0).get("eventId").asString()).isEqualTo(id.toString());
        assertThat(matches.findById(new MatchId(match))).isEmpty();
    }

    @Test
    void aBrokenEventWithIdIsRejectedAndNotSaved() throws Exception {
        UUID id = UUID.randomUUID();

        publish(topic(), event(id, 1, "GoalScored", "{\"side\":\"GREEN\"}"));
        JsonNode ack = nextAck();

        assertThat(ack.get("rejected").get(0).get("eventId").asString()).isEqualTo(id.toString());
        assertThat(matches.findById(new MatchId(match))).isEmpty();
    }

    @Test
    void aMessageTheDatabaseRefusesDoesNotBlockTheNextOnes() throws Exception {
        // the parser accepts it, but PostgreSQL does not accept a NUL character in a text column
        UUID poison = UUID.randomUUID();
        publish(topic(), event(poison, 1, "MatchStarted",
                "{\"gameTypeId\":\"00000000-0000-4000-8000-0000000000b1\",\"mode\":\"INDIVIDUAL\",\"pointsToWin\":5,\"sides\":[{\"side\":\"RED\",\"displayName\":\"bad\\u0000name\"},{\"side\":\"BLUE\"}]}"));
        UUID otherMatch = UUID.randomUUID();
        UUID good = UUID.randomUUID();
        publish("venues/%s/tables/%s/matches/%s/events".formatted(venue, table, otherMatch),
                event(good, 1, "MatchStarted", "{\"gameTypeId\":\"00000000-0000-4000-8000-0000000000b1\",\"mode\":\"INDIVIDUAL\",\"pointsToWin\":5,\"sides\":[{\"side\":\"RED\"},{\"side\":\"BLUE\"}]}")
                        .replace(match.toString(), otherMatch.toString()));

        JsonNode ack = nextAck();

        // the first ack is the one of the good event: the poison got none, and did not stop the queue
        assertThat(ack.get("acknowledged").get(0).asString()).isEqualTo(good.toString());
        assertThat(matches.findById(new MatchId(match))).isEmpty();
        assertThat(matches.findById(new MatchId(otherMatch))).isPresent();
    }
}
