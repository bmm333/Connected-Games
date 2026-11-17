package com.connectedgames.match;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** The whole way: the hub publishes on MQTT, the service saves it, and a screen on HTTP sees the score change. */
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TableLiveIntegrationTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Value("${match.mqtt.url}") String brokerUrl;

    private final UUID venue = UUID.randomUUID();
    private final UUID table = UUID.randomUUID();
    private final UUID match = UUID.randomUUID();
    private final BlockingQueue<String> acks = new LinkedBlockingQueue<>();
    private final BlockingQueue<JsonNode> screen = new LinkedBlockingQueue<>();
    private MqttClient hub;
    private Thread reader;

    @BeforeEach
    void connectTheHub() throws Exception {
        hub = new MqttClient(brokerUrl, "live-hub-" + UUID.randomUUID(), null);
        hub.connect();
        hub.subscribe("venues/" + venue + "/hub/ack", 1,
                (topic, message) -> acks.add(new String(message.getPayload(), StandardCharsets.UTF_8)));
    }

    @AfterEach
    void closeEverything() throws Exception {
        hub.disconnect();
        hub.close();
        if (reader != null) {
            reader.interrupt();
        }
    }

    /** A screen: GET on the stream, every "data:" line is a Match that goes to the queue. */
    private void openTheScreen() {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:%d/api/match/tables/%s/live".formatted(port, table)))
                .header("Accept", "text/event-stream").build();
        reader = Thread.ofPlatform().daemon().start(() -> {
            try {
                HttpResponse<java.util.stream.Stream<String>> response =
                        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofLines());
                response.body().filter(line -> line.startsWith("data:"))
                        .forEach(line -> screen.add(JSON.readTree(line.substring("data:".length()))));
            } catch (Exception e) {
                // closed at the end of the test
            }
        });
    }

    private JsonNode nextOnScreen() throws Exception {
        JsonNode match = screen.poll(10, TimeUnit.SECONDS);
        assertThat(match).as("a Match on the stream within 10 seconds").isNotNull();
        return match;
    }

    private void publish(int seq, String type, String payload) throws Exception {
        String json = """
                {"eventId":"%s","tenantId":"acme","venueId":"%s","tableId":"%s","matchId":"%s",
                 "seq":%d,"type":"%s","occurredAt":"2026-11-16T20:00:00Z","source":"CAMERA","payload":%s}"""
                .formatted(UUID.randomUUID(), venue, table, match, seq, type, payload);
        hub.publish("venues/%s/tables/%s/matches/%s/events".formatted(venue, table, match),
                json.getBytes(StandardCharsets.UTF_8), 1, false);
        assertThat(acks.poll(10, TimeUnit.SECONDS)).as("ack of event " + seq).isNotNull();
    }

    private static int score(JsonNode match, String side) {
        for (JsonNode s : match.get("sides")) {
            if (s.get("side").asString().equals(side)) {
                return s.get("score").asInt();
            }
        }
        throw new AssertionError("no side " + side);
    }

    @Test
    void aScreenSeesTheMatchStartTheGoalsAndTheEnd() throws Exception {
        openTheScreen(); // nothing is playing: the stream opens and stays silent
        assertThat(screen.poll(1, TimeUnit.SECONDS)).isNull();

        publish(1, "MatchStarted",
                "{\"mode\":\"INDIVIDUAL\",\"pointsToWin\":2,\"sides\":[{\"side\":\"RED\"},{\"side\":\"BLUE\"}]}");
        JsonNode started = nextOnScreen();
        assertThat(started.get("id").asString()).isEqualTo(match.toString());
        assertThat(started.get("status").asString()).isEqualTo("IN_PROGRESS");

        publish(2, "GoalScored", "{\"side\":\"RED\"}");
        assertThat(score(nextOnScreen(), "RED")).isEqualTo(1);

        publish(3, "GoalScored", "{\"side\":\"RED\"}");
        assertThat(score(nextOnScreen(), "RED")).isEqualTo(2);

        publish(4, "MatchEnded", "{\"reason\":\"SCORE_REACHED\",\"winnerSide\":\"RED\","
                + "\"finalScore\":[{\"side\":\"RED\",\"score\":2},{\"side\":\"BLUE\",\"score\":0}]}");
        JsonNode ended = nextOnScreen();
        assertThat(ended.get("status").asString()).isEqualTo("COMPLETED");
        assertThat(ended.get("winnerSide").asString()).isEqualTo("RED");
    }

    @Test
    void aScreenThatOpensDuringTheMatchGetsTheScoreAtOnce() throws Exception {
        publish(1, "MatchStarted",
                "{\"mode\":\"INDIVIDUAL\",\"pointsToWin\":5,\"sides\":[{\"side\":\"RED\"},{\"side\":\"BLUE\"}]}");
        publish(2, "GoalScored", "{\"side\":\"BLUE\"}");

        openTheScreen();

        JsonNode current = nextOnScreen();
        assertThat(score(current, "BLUE")).isEqualTo(1);
        assertThat(score(current, "RED")).isZero();
    }
}
