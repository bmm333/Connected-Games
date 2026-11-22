package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.Fixtures;
import com.connectedgames.match.TestcontainersConfiguration;
import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.match.MatchSide;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The outbox on a real PostgreSQL migrated by Flyway. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, OutboxPersistenceAdapter.class})
class OutboxPersistenceAdapterTest {

    private static final Instant PUBLISHED_AT = Instant.parse("2026-11-20T10:00:00Z");

    @Autowired OutboxPersistenceAdapter outbox;
    @Autowired TestEntityManager em;
    @Autowired JdbcTemplate jdbc;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private Instant publishedAtOf(EventId id) {
        return jdbc.queryForObject("select published_at from match.outbox_event where event_id = ?",
                java.sql.Timestamp.class, id.value()).toInstant();
    }

    @Test
    void savesAndLoadsAMessageWithEveryField() {
        MatchCompleted message = Fixtures.completed();
        outbox.add(message);
        flushAndClear();

        assertThat(outbox.unpublished(10)).containsExactly(message);
    }

    @Test
    void savesAndLoadsAFriendlyDrawWithoutTheOptionalFields() {
        MatchCompleted draw = new MatchCompleted(new EventId(UUID.randomUUID()), Fixtures.TENANT,
                Fixtures.completed().venueId(), Fixtures.completed().tableId(), new MatchId(UUID.randomUUID()),
                Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5, Instant.parse("2026-11-16T20:00:00Z"),
                Instant.parse("2026-11-16T20:10:00Z"), EndReason.TIME_EXPIRED, null,
                List.of(new MatchSide(Side.RED, null, null, 2), new MatchSide(Side.BLUE, null, null, 2)));
        outbox.add(draw);
        flushAndClear();

        MatchCompleted loaded = outbox.unpublished(10).get(0);

        assertThat(loaded).isEqualTo(draw);
        assertThat(loaded.tournamentMatchId()).isNull();
        assertThat(loaded.winnerSide()).isNull();
    }

    @Test
    void theOldestMessageComesFirst() {
        MatchCompleted first = Fixtures.completed();
        MatchCompleted second = Fixtures.completed();
        MatchCompleted third = Fixtures.completed();
        outbox.add(first);
        outbox.add(second);
        outbox.add(third);
        flushAndClear();

        assertThat(outbox.unpublished(10)).containsExactly(first, second, third);
    }

    @Test
    void returnsAtMostTheLimit() {
        MatchCompleted first = Fixtures.completed();
        outbox.add(first);
        outbox.add(Fixtures.completed());
        flushAndClear();

        assertThat(outbox.unpublished(1)).containsExactly(first);
    }

    @Test
    void aPublishedMessageLeavesTheListButTheRowStays() {
        MatchCompleted first = Fixtures.completed();
        MatchCompleted second = Fixtures.completed();
        outbox.add(first);
        outbox.add(second);
        flushAndClear();

        outbox.markPublished(first.eventId(), PUBLISHED_AT);
        flushAndClear();

        assertThat(outbox.unpublished(10)).containsExactly(second);
        assertThat(publishedAtOf(first.eventId())).isEqualTo(PUBLISHED_AT);
    }

    @Test
    void publishingTwiceKeepsTheFirstTime() {
        MatchCompleted message = Fixtures.completed();
        outbox.add(message);
        flushAndClear();

        outbox.markPublished(message.eventId(), PUBLISHED_AT);
        outbox.markPublished(message.eventId(), PUBLISHED_AT.plusSeconds(60));
        flushAndClear();

        assertThat(publishedAtOf(message.eventId())).isEqualTo(PUBLISHED_AT);
    }

    @Test
    void markingAnUnknownMessageDoesNothing() {
        outbox.markPublished(new EventId(UUID.randomUUID()), PUBLISHED_AT);

        assertThat(outbox.unpublished(10)).isEmpty();
    }

    @Test
    void aMatchKeepsOnlyItsFirstMessage() {
        MatchId match = new MatchId(UUID.randomUUID());
        MatchCompleted first = Fixtures.completed(match);
        outbox.add(first);
        flushAndClear();

        outbox.add(Fixtures.completed(match));
        flushAndClear();

        assertThat(outbox.unpublished(10)).containsExactly(first);
    }

    @Test
    void theDatabaseRefusesASecondMessageForTheSameMatch() {
        MatchId match = new MatchId(UUID.randomUUID());
        String insert = "insert into match.outbox_event (event_id, type, match_id, payload) values (?, 'MatchCompleted', ?, '{}')";
        jdbc.update(insert, UUID.randomUUID(), match.value());

        assertThatThrownBy(() -> jdbc.update(insert, UUID.randomUUID(), match.value()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
