package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.Fixtures;
import com.connectedgames.match.TestcontainersConfiguration;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideScore;
import com.connectedgames.match.domain.event.SideSetup;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchSide;
import com.connectedgames.match.domain.match.MatchStatus;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Saves and loads through the adapters on a real PostgreSQL migrated by Flyway. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, MatchPersistenceAdapter.class, MatchEventPersistenceAdapter.class})
class MatchPersistenceAdaptersTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-16T20:00:00Z");

    @Autowired MatchPersistenceAdapter matches;
    @Autowired MatchEventPersistenceAdapter events;
    @Autowired TestEntityManager em;

    /** Writes to the database and empties the JPA cache, so the next read really hits the tables */
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    private static MatchStarted started(VenueId venue, TableId table, MatchId match, int seq, Instant at) {
        return new MatchStarted(newId(), venue, table, match, seq, at, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.TEAM, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, "team-7", "Lightning")));
    }

    private static MatchStarted started(int seq) {
        return started(VENUE, TABLE, MATCH, seq, NOW);
    }

    private static GoalScored goal(VenueId venue, MatchId match, int seq, Side side) {
        return new GoalScored(newId(), venue, TABLE, match, seq, NOW, side);
    }

    private static GoalScored goal(int seq, Side side) {
        return goal(VENUE, MATCH, seq, side);
    }

    private static MatchEnded ended(int seq, int red, int blue) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW.plusSeconds(600), EndReason.SCORE_REACHED,
                List.of(new SideScore(RED, red), new SideScore(BLUE, blue)), red > blue ? RED : BLUE);
    }

    private static Match matchOf(MatchEvent... events) {
        return Match.replay(List.of(events)).orElseThrow();
    }

    // ---- the event log

    @Test
    void savesAndLoadsEveryKindOfEvent() {
        List<MatchEvent> all = List.of(started(1), goal(2, RED), ended(3, 5, 3));
        all.forEach(events::save);
        flushAndClear();

        assertThat(events.findByMatchId(VENUE, MATCH)).isEqualTo(all);
    }

    @Test
    void eventsComeBackInSequenceOrder() {
        MatchStarted start = started(1);
        GoalScored first = goal(2, RED);
        GoalScored second = goal(3, BLUE);
        events.save(second);
        events.save(start);
        events.save(first);
        flushAndClear();

        assertThat(events.findByMatchId(VENUE, MATCH)).containsExactly(start, first, second);
    }

    @Test
    void onlyTheEventsOfThatMatchAreLoaded() {
        MatchId other = new MatchId(UUID.randomUUID());
        events.save(started(1));
        events.save(goal(VENUE, other, 1, RED));
        flushAndClear();

        assertThat(events.findByMatchId(VENUE, MATCH)).hasSize(1);
    }

    @Test
    void knowsWhichEventsItAlreadyHas() {
        MatchStarted start = started(1);
        events.save(start);
        flushAndClear();

        assertThat(events.existsById(start.id())).isTrue();
        assertThat(events.existsById(newId())).isFalse();
    }

    @Test
    void aSeqIsTakenOnlyInsideItsOwnVenue() {
        events.save(started(1));
        flushAndClear();

        assertThat(events.existsByMatchAndSeq(VENUE, MATCH, 1)).isTrue();
        assertThat(events.existsByMatchAndSeq(VENUE, MATCH, 2)).isFalse();
        assertThat(events.existsByMatchAndSeq(new VenueId(UUID.randomUUID()), MATCH, 1)).isFalse();
    }

    @Test
    void anotherVenueCanUseTheSameSeqOfTheSameMatchId() {
        events.save(goal(new VenueId(UUID.randomUUID()), MATCH, 1, RED));
        events.save(started(1));

        em.flush();     // no unique violation: the match is venue + match id
    }

    @Test
    void theDatabaseRefusesTheSameSeqTwiceInAVenue() {
        events.save(started(1));
        events.save(goal(1, RED));

        assertThatThrownBy(() -> em.flush()).isInstanceOf(PersistenceException.class);
    }

    // ---- the matches

    @Test
    void savesAndLoadsAMatchWithItsSides() {
        Match match = matchOf(started(1), goal(2, BLUE));
        matches.save(match);
        flushAndClear();

        Match loaded = matches.findById(MATCH).orElseThrow();

        assertThat(loaded).isEqualTo(match);
        assertThat(loaded.sides()).extracting(MatchSide::side).containsExactly(RED, BLUE);
        assertThat(loaded.sides().get(0).participantId()).isNull();
        assertThat(loaded.sides().get(1).participantId()).isEqualTo("team-7");
    }

    @Test
    void savesAndLoadsTenantGameTypeAndTournamentMatch() {
        Match match = matchOf(new MatchStarted(newId(), VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE,
                Fixtures.TOURNAMENT_MATCH, GameMode.TEAM, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue"))));
        matches.save(match);
        flushAndClear();

        Match loaded = matches.findById(MATCH).orElseThrow();

        assertThat(loaded.tenantId()).isEqualTo(Fixtures.TENANT);
        assertThat(loaded.gameTypeId()).isEqualTo(Fixtures.GAME_TYPE);
        assertThat(loaded.tournamentMatchId()).isEqualTo(Fixtures.TOURNAMENT_MATCH);
    }

    @Test
    void aFriendlyMatchIsSavedWithoutTournamentMatch() {
        matches.save(matchOf(started(1)));
        flushAndClear();

        assertThat(matches.findById(MATCH).orElseThrow().tournamentMatchId()).isNull();
    }

    @Test
    void theLogKeepsTenantGameTypeAndTournamentMatchOfTheStart() {
        MatchStarted inTournament = new MatchStarted(newId(), VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT,
                Fixtures.GAME_TYPE, Fixtures.TOURNAMENT_MATCH, GameMode.TEAM, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
        events.save(inTournament);
        flushAndClear();

        assertThat(events.findByMatchId(VENUE, MATCH)).containsExactly(inTournament);
    }

    @Test
    void savingAgainUpdatesTheScoreWithoutDuplicatingTheSides() {
        MatchStarted start = started(1);
        matches.save(matchOf(start));
        flushAndClear();

        matches.save(matchOf(start, goal(2, RED), goal(3, RED)));
        flushAndClear();

        Match loaded = matches.findById(MATCH).orElseThrow();
        assertThat(loaded.sides()).hasSize(2);
        assertThat(loaded.scoreOf(RED)).isEqualTo(2);
    }

    @Test
    void anEndedMatchKeepsItsWinnerAndItsEnd() {
        matches.save(matchOf(started(1), ended(2, 5, 3)));
        flushAndClear();

        Match loaded = matches.findById(MATCH).orElseThrow();

        assertThat(loaded.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(loaded.winnerSide()).isEqualTo(RED);
        assertThat(loaded.endedAt()).isEqualTo(NOW.plusSeconds(600));
        assertThat(loaded.scoreOf(BLUE)).isEqualTo(3);
    }

    @Test
    void anUnknownMatchIsEmpty() {
        assertThat(matches.findById(new MatchId(UUID.randomUUID()))).isEmpty();
        assertThat(matches.findByIdForUpdate(new MatchId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void theMatchCanBeLockedForAnUpdate() {
        Match match = matchOf(started(1));
        matches.save(match);
        flushAndClear();

        assertThat(matches.findByIdForUpdate(MATCH)).contains(match);
    }

    @Test
    void theCurrentMatchOfATableIsTheOneInProgress() {
        MatchId finished = new MatchId(UUID.randomUUID());
        matches.save(Match.replay(List.of(started(VENUE, TABLE, finished, 1, NOW.minusSeconds(3600)),
                new MatchEnded(newId(), VENUE, TABLE, finished, 2, NOW.minusSeconds(3000), EndReason.SCORE_REACHED,
                        List.of(new SideScore(RED, 5), new SideScore(BLUE, 1)), RED))).orElseThrow());
        Match inProgress = matchOf(started(1));
        matches.save(inProgress);
        flushAndClear();

        assertThat(matches.findCurrentByTable(TABLE)).contains(inProgress);
        assertThat(matches.findCurrentByTable(new TableId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void ifTwoAreInProgressTheLatestStartedIsTheCurrentOne() {
        MatchId older = new MatchId(UUID.randomUUID());
        matches.save(Match.replay(List.of(started(VENUE, TABLE, older, 1, NOW.minusSeconds(7200)))).orElseThrow());
        Match newer = matchOf(started(1));
        matches.save(newer);
        flushAndClear();

        assertThat(matches.findCurrentByTable(TABLE)).contains(newer);
    }
}
