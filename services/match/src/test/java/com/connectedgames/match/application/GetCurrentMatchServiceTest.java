package com.connectedgames.match.application;

import com.connectedgames.match.Fixtures;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideScore;
import com.connectedgames.match.domain.event.SideSetup;
import com.connectedgames.match.domain.match.Match;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCurrentMatchServiceTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private final InMemoryMatchRepository matches = new InMemoryMatchRepository();
    private final GetCurrentMatchService service = new GetCurrentMatchService(matches);

    private static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    private static MatchStarted started(TableId table, MatchId match) {
        return new MatchStarted(newId(), VENUE, table, match, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
    }

    private static MatchEnded ended(TableId table, MatchId match) {
        return new MatchEnded(newId(), VENUE, table, match, 2, NOW, EndReason.SCORE_REACHED,
                List.of(new SideScore(RED, 5), new SideScore(BLUE, 3)), RED);
    }

    private Match save(Match match) {
        matches.save(match);
        return match;
    }

    @Test
    void returnsTheMatchInProgressAtTheTable() {
        MatchId id = new MatchId(UUID.randomUUID());
        Match inProgress = save(Match.replay(List.of(started(TABLE, id))).orElseThrow());

        assertThat(service.getCurrentMatch(TABLE)).isEqualTo(inProgress);
    }

    @Test
    void anEmptyTableIsNotFound() {
        assertThatThrownBy(() -> service.getCurrentMatch(TABLE)).isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void aFinishedMatchIsNotTheCurrentOne() {
        MatchId id = new MatchId(UUID.randomUUID());
        save(Match.replay(List.of(started(TABLE, id), ended(TABLE, id))).orElseThrow());

        assertThatThrownBy(() -> service.getCurrentMatch(TABLE)).isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void aMatchAtAnotherTableIsNotReturned() {
        TableId other = new TableId(UUID.randomUUID());
        save(Match.replay(List.of(started(other, new MatchId(UUID.randomUUID())))).orElseThrow());

        assertThatThrownBy(() -> service.getCurrentMatch(TABLE)).isInstanceOf(MatchNotFoundException.class);
    }
}
