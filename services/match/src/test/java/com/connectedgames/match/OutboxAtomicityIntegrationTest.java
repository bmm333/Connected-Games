package com.connectedgames.match;

import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideScore;
import com.connectedgames.match.domain.event.SideSetup;
import com.connectedgames.match.domain.match.MatchStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;

/**
 * The closing of a match and its message in the outbox are one transaction (ADR011): real database,
 * and an outbox that fails on purpose.
 */
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
@SpringBootTest
class OutboxAtomicityIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-16T20:00:00Z");

    @Autowired RecordMatchEventUseCase record;
    @Autowired MatchEventRepository events;
    @Autowired MatchRepository matches;
    @MockitoSpyBean MatchCompletedOutbox outbox;

    private final VenueId venue = new VenueId(UUID.randomUUID());
    private final TableId table = new TableId(UUID.randomUUID());
    private final MatchId match = new MatchId(UUID.randomUUID());
    private final EventTopic topic = new EventTopic(venue, table, match);

    private MatchStarted started() {
        return new MatchStarted(new EventId(UUID.randomUUID()), venue, table, match, 1, NOW, Fixtures.TENANT,
                Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(Side.RED, null, null), new SideSetup(Side.BLUE, null, null)));
    }

    private MatchEnded ended() {
        return new MatchEnded(new EventId(UUID.randomUUID()), venue, table, match, 2, NOW.plusSeconds(600),
                EndReason.SCORE_REACHED, List.of(new SideScore(Side.RED, 5), new SideScore(Side.BLUE, 3)), Side.RED);
    }

    @Test
    void ifTheMessageCannotBeWrittenTheEndIsNotSavedEither() {
        assertThat(record.record(started(), topic)).isInstanceOf(RecordResult.Accepted.class);
        doThrow(new IllegalStateException("outbox down")).when(outbox).add(any());
        MatchEnded end = ended();

        assertThatThrownBy(() -> record.record(end, topic)).isInstanceOf(IllegalStateException.class);

        assertThat(events.existsById(end.id())).isFalse();
        assertThat(matches.findById(match).orElseThrow().status()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    void theHubSendsTheEndAgainAndThenTheMatchIsClosed() {
        record.record(started(), topic);
        MatchEnded end = ended();
        doThrow(new IllegalStateException("outbox down")).when(outbox).add(any());
        assertThatThrownBy(() -> record.record(end, topic)).isInstanceOf(IllegalStateException.class);

        doCallRealMethod().when(outbox).add(any());
        RecordResult again = record.record(end, topic);

        assertThat(again).isInstanceOf(RecordResult.Accepted.class);
        assertThat(matches.findById(match).orElseThrow().status()).isEqualTo(MatchStatus.COMPLETED);
    }
}
