package com.connectedgames.stats;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The whole way: a message on the broker, the service, the database. */
@SpringBootTest
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
class MatchCompletedIntegrationTest {

    private static final String TOPIC = "platform/matches/completed";

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MqttPahoMessageDrivenChannelAdapter adapter;
    @Value("${stats.mqtt.url}")
    String mqttUrl;

    private MqttClient matchService;

    @BeforeEach
    void connectAsTheMatchServiceAndWaitForStats() throws Exception {
        matchService = new MqttClient(mqttUrl, "test-match-service-" + UUID.randomUUID(), new MemoryPersistence());
        matchService.connect();
        // The subscription of stats is made in the background when the context starts: send a probe until it is heard
        UUID probe = UUID.randomUUID();
        long deadline = System.currentTimeMillis() + 20_000;
        while (!isSaved(probe)) {
            assertThat(System.currentTimeMillis()).as("stats listening within 20 seconds").isLessThan(deadline);
            publish(Messages.completed(probe));
            Thread.sleep(300);
        }
        jdbc.update("delete from match_result");
    }

    @AfterEach
    void disconnect() throws Exception {
        if (!adapter.isRunning()) {
            adapter.start();
        }
        matchService.disconnect();
        matchService.close();
    }

    private void publish(String json) throws Exception {
        matchService.publish(TOPIC, json.getBytes(StandardCharsets.UTF_8), 1, false);
    }

    private boolean isSaved(UUID match) {
        return jdbc.queryForObject("select count(*) from match_result where match_id = ?", Integer.class, match) > 0;
    }

    private void awaitSaved(UUID match) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        while (!isSaved(match)) {
            assertThat(System.currentTimeMillis()).as("match " + match + " saved within 10 seconds").isLessThan(deadline);
            Thread.sleep(100);
        }
    }

    private int rows(String table, UUID match) {
        return jdbc.queryForObject("select count(*) from " + table + " where match_id = ?", Integer.class, match);
    }

    @Test
    void aClosedMatchOnTheBrokerEndsUpInTheDatabase() throws Exception {
        UUID match = UUID.randomUUID();

        publish(Messages.completed(match));
        awaitSaved(match);

        assertThat(jdbc.queryForObject("select winner_side from match_result where match_id = ?", String.class, match))
                .isEqualTo("RED");
        assertThat(rows("match_side_result", match)).isEqualTo(2);
        assertThat(rows("scored_point", match)).isEqualTo(3);
    }

    @Test
    void theSameMessageSentTwiceIsSavedOnce() throws Exception {
        UUID match = UUID.randomUUID();
        UUID sentinel = UUID.randomUUID();

        publish(Messages.completed(match));
        publish(Messages.completed(match));
        publish(Messages.completed(sentinel));
        awaitSaved(sentinel);

        assertThat(rows("match_result", match)).isEqualTo(1);
        assertThat(rows("scored_point", match)).isEqualTo(3);
    }

    @Test
    void aCancelledMatchIsNotSaved() throws Exception {
        UUID cancelled = UUID.randomUUID();
        UUID sentinel = UUID.randomUUID();

        publish(Messages.cancelled(cancelled));
        publish(Messages.completed(sentinel));
        awaitSaved(sentinel);

        assertThat(rows("match_result", cancelled)).isZero();
    }

    @Test
    void aMessageNobodyCanReadDoesNotBlockTheNextOnes() throws Exception {
        UUID match = UUID.randomUUID();

        publish("not json");
        publish(Messages.completed(UUID.randomUUID()).replace("SCORE_REACHED", "GOLDEN_GOAL"));
        publish(Messages.draw(match));
        awaitSaved(match);

        assertThat(rows("match_result", match)).isEqualTo(1);
    }

    @Test
    void aMessageSentWhileStatsIsStoppedArrivesWhenItStartsAgain() throws Exception {
        UUID whileDown = UUID.randomUUID();
        // Stopping the adapter is what a graceful shutdown does
        adapter.stop();
        publish(Messages.completed(whileDown));
        Thread.sleep(500);
        assertThat(isSaved(whileDown)).isFalse();

        adapter.start();

        awaitSaved(whileDown);
    }
}
