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

class GetMatchServiceTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private final InMemoryMatchRepository matches = new InMemoryMatchRepository();
    private final GetMatchService service = new GetMatchService(matches);

    private static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    private static MatchStarted started(MatchId match) {
        return new MatchStarted(newId(), VENUE, TABLE, match, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
    }

    private static MatchEnded ended(MatchId match) {
        return new MatchEnded(newId(), VENUE, TABLE, match, 2, NOW, EndReason.SCORE_REACHED,
                List.of(new SideScore(RED, 5), new SideScore(BLUE, 3)), RED);
    }

    @Test
    void returnsTheMatch() {
        MatchId id = new MatchId(UUID.randomUUID());
        Match match = Match.replay(List.of(started(id))).orElseThrow();
        matches.save(match);

        assertThat(service.getMatch(id)).isEqualTo(match);
    }

    @Test
    void aFinishedMatchCanStillBeRead() {
        MatchId id = new MatchId(UUID.randomUUID());
        Match match = Match.replay(List.of(started(id), ended(id))).orElseThrow();
        matches.save(match);

        assertThat(service.getMatch(id)).isEqualTo(match);
    }

    @Test
    void anUnknownMatchIsNotFound() {
        assertThatThrownBy(() -> service.getMatch(new MatchId(UUID.randomUUID())))
                .isInstanceOf(MatchNotFoundException.class);
    }
}
