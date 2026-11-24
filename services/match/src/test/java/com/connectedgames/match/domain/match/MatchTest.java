package com.connectedgames.match.domain.match;

import com.connectedgames.match.Fixtures;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.match.domain.*;
import com.connectedgames.match.domain.event.*;

import static com.connectedgames.match.domain.Side.BLUE;
import static com.connectedgames.match.domain.Side.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class MatchTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private static EventId newId() { return new EventId(UUID.randomUUID()); }

    private static MatchStarted started(int seq) {
        return new MatchStarted(newId(), VENUE, TABLE, MATCH, seq, NOW, Fixtures.TENANT, Fixtures.GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));
    }

    private static GoalScored goal(int seq, Side side) {
        return new GoalScored(newId(), VENUE, TABLE, MATCH, seq, NOW, side);
    }

    private static MatchEnded ended(int seq, int red, int blue) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW, EndReason.SCORE_REACHED,
                List.of(new SideScore(RED, red), new SideScore(BLUE, blue)), red > blue ? RED : BLUE);
    }

    private static MatchEnded cancelled(int seq) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW, EndReason.CANCELLED,
                List.of(new SideScore(RED, 0), new SideScore(BLUE, 0)), null);
    }

    private static MatchEnded drawAfterTime(int seq) {
        return new MatchEnded(newId(), VENUE, TABLE, MATCH, seq, NOW, EndReason.TIME_EXPIRED,
                List.of(new SideScore(RED, 2), new SideScore(BLUE, 2)), null);
    }

    @Test
    void theOrderOfArrivalDoesNotMatter() {
        List<MatchEvent> inOrder = List.of(started(1), goal(2, RED), goal(3, BLUE), goal(4, RED));
        List<MatchEvent> shuffled = List.of(inOrder.get(3), inOrder.get(0), inOrder.get(2), inOrder.get(1));

        assertThat(Match.replay(shuffled)).isEqualTo(Match.replay(inOrder));
    }

    @Test
    void theMatchKeepsWhatTheStartSaidAboutTenantGameTypeAndTournament() {
        MatchStarted inTournament = new MatchStarted(newId(), VENUE, TABLE, MATCH, 1, NOW, Fixtures.TENANT,
                Fixtures.GAME_TYPE, Fixtures.TOURNAMENT_MATCH, GameMode.TEAM, 5,
                List.of(new SideSetup(RED, null, "Red"), new SideSetup(BLUE, null, "Blue")));

        Match match = Match.replay(List.of(inTournament, goal(2, RED), ended(3, 5, 3))).orElseThrow();

        assertThat(match.tenantId()).isEqualTo(Fixtures.TENANT);
        assertThat(match.gameTypeId()).isEqualTo(Fixtures.GAME_TYPE);
        assertThat(match.tournamentMatchId()).isEqualTo(Fixtures.TOURNAMENT_MATCH);
    }

    @Test
    void aFriendlyMatchHasNoTournamentMatch() {
        Match match = Match.replay(List.of(started(1))).orElseThrow();

        assertThat(match.tenantId()).isEqualTo(Fixtures.TENANT);
        assertThat(match.tournamentMatchId()).isNull();
    }

    @Test
    void aMatchInProgressHasNoEndReasonAndIsNotClosed() {
        Match match = Match.replay(List.of(started(1), goal(2, RED))).orElseThrow();

        assertThat(match.endReason()).isNull();
        assertThat(match.isClosed()).isFalse();
    }

    @Test
    void aClosedMatchKnowsWhyItEnded() {
        assertThat(Match.replay(List.of(started(1), ended(2, 5, 3))).orElseThrow().endReason())
                .isEqualTo(EndReason.SCORE_REACHED);
        assertThat(Match.replay(List.of(started(1), drawAfterTime(2))).orElseThrow().endReason())
                .isEqualTo(EndReason.TIME_EXPIRED);
        Match cancelled = Match.replay(List.of(started(1), cancelled(2))).orElseThrow();
        assertThat(cancelled.endReason()).isEqualTo(EndReason.CANCELLED);
        assertThat(cancelled.isClosed()).isTrue();
    }

    @Test
    void theFirstEndIsTheOneThatCountsForTheReason() {
        Match match = Match.replay(List.of(started(1), ended(2, 5, 3), cancelled(3))).orElseThrow();

        assertThat(match.endReason()).isEqualTo(EndReason.SCORE_REACHED);
    }

    @Test
    void withoutTheStartThereIsNoMatchYet() {
        assertThat(Match.replay(List.of(goal(2, RED)))).isEmpty();
    }

    @Test
    void theFinalScoreOfTheEdgeWins() {
        Match match = Match.replay(List.of(started(1), goal(2, RED), ended(9, 5, 3))).orElseThrow();

        assertThat(match.scoreOf(RED)).isEqualTo(5);
        assertThat(match.scoreOf(BLUE)).isEqualTo(3);
    }

    @Test
    void startsInProgressAtZero() {
        Match match = Match.replay(List.of(started(1))).orElseThrow();

        assertThat(match.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match.scoreOf(RED)).isZero();
        assertThat(match.scoreOf(BLUE)).isZero();
        assertThat(match.startedAt()).isEqualTo(NOW);
        assertThat(match.endedAt()).isNull();
        assertThat(match.winnerSide()).isNull();
    }

    @Test
    void aGoalCountsOnlyForItsSide() {
        Match match = Match.replay(List.of(started(1), goal(2, RED), goal(3, RED), goal(4, BLUE))).orElseThrow();

        assertThat(match.scoreOf(RED)).isEqualTo(2);
        assertThat(match.scoreOf(BLUE)).isEqualTo(1);
    }

    @Test
    void aGapKeepsWhatHasArrived() {
        Match match = Match.replay(List.of(started(1), goal(2, RED), goal(4, BLUE))).orElseThrow();

        assertThat(match.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match.scoreOf(RED)).isEqualTo(1);
        assertThat(match.scoreOf(BLUE)).isEqualTo(1);
    }

    @Test
    void theMissingEventArrivesLater() {
        List<MatchEvent> arrived = new ArrayList<>(List.of(started(1), goal(2, RED), goal(4, BLUE)));
        assertThat(Match.replay(arrived).orElseThrow().scoreOf(RED)).isEqualTo(1);

        arrived.add(goal(3, RED));
        Match complete = Match.replay(arrived).orElseThrow();

        assertThat(complete.scoreOf(RED)).isEqualTo(2);
        assertThat(complete.scoreOf(BLUE)).isEqualTo(1);
    }

    @Test
    void scoreReachedCompletesTheMatchWithItsWinner() {
        Match match = Match.replay(List.of(started(1), ended(2, 5, 3))).orElseThrow();

        assertThat(match.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(match.winnerSide()).isEqualTo(RED);
        assertThat(match.endedAt()).isEqualTo(NOW);
    }

    @Test
    void cancelledHasNoWinner() {
        Match match = Match.replay(List.of(started(1), cancelled(2))).orElseThrow();

        assertThat(match.status()).isEqualTo(MatchStatus.CANCELLED);
        assertThat(match.winnerSide()).isNull();
    }

    @Test
    void aDrawAfterTimeIsCompletedWithoutWinner() {
        Match match = Match.replay(List.of(started(1), drawAfterTime(2))).orElseThrow();

        assertThat(match.status()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(match.winnerSide()).isNull();
    }

    @Test
    void aGoalAfterTheEndIsIgnored() {
        Match match = Match.replay(List.of(started(1), ended(2, 1, 0), goal(3, BLUE))).orElseThrow();

        assertThat(match.scoreOf(RED)).isEqualTo(1);
        assertThat(match.scoreOf(BLUE)).isZero();
    }

    @Test
    void onlyTheFirstEndCounts() {
        Match match = Match.replay(List.of(started(1), ended(2, 5, 3), ended(3, 0, 5))).orElseThrow();

        assertThat(match.scoreOf(RED)).isEqualTo(5);
        assertThat(match.winnerSide()).isEqualTo(RED);
    }

    @Test
    void aSecondStartDoesNotChangeTheMatch() {
        Match match = Match.replay(List.of(started(1), goal(2, RED), started(3))).orElseThrow();

        assertThat(match.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match.scoreOf(RED)).isEqualTo(1);
    }

    @Test
    void eventsOfAnotherVenueAreIgnored() {
        VenueId other = new VenueId(UUID.randomUUID());
        GoalScored foreign = new GoalScored(newId(), other, TABLE, MATCH, 2, NOW, RED);

        Match match = Match.replay(List.of(started(1), foreign)).orElseThrow();

        assertThat(match.scoreOf(RED)).isZero();
    }

    @Test
    void eventsOfAnotherTableAreIgnored() {
        TableId other = new TableId(UUID.randomUUID());
        GoalScored foreign = new GoalScored(newId(), VENUE, other, MATCH, 2, NOW, RED);

        Match match = Match.replay(List.of(started(1), foreign)).orElseThrow();

        assertThat(match.scoreOf(RED)).isZero();
    }

    // ---- the points that counted

    @Test
    void thePointsAreTheGoalsInOrderOfSeqWhateverTheOrderOfArrival() {
        List<MatchEvent> events = List.of(goal(4, RED), ended(5, 2, 1), started(1), goal(3, BLUE), goal(2, RED));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).extracting(ScoredPoint::seq, ScoredPoint::side)
                .containsExactly(tuple(2, RED), tuple(3, BLUE), tuple(4, RED));
    }

    @Test
    void aPointKeepsTheTimeOfItsGoal() {
        Instant later = NOW.plusSeconds(90);
        List<MatchEvent> events = List.of(started(1), new GoalScored(newId(), VENUE, TABLE, MATCH, 2, later, RED),
                ended(3, 1, 0));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).containsExactly(new ScoredPoint(2, RED, later));
    }

    @Test
    void aGoalAfterTheEndIsNotAPoint() {
        List<MatchEvent> events = List.of(started(1), goal(2, RED), ended(3, 1, 0), goal(4, BLUE));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).extracting(ScoredPoint::seq).containsExactly(2);
    }

    @Test
    void aLateGoalWithALowerSeqThanTheEndIsAPoint() {
        List<MatchEvent> events = List.of(started(1), ended(5, 2, 1), goal(3, BLUE), goal(2, RED));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).extracting(ScoredPoint::seq).containsExactly(2, 3);
    }

    @Test
    void pointsStopAtTheFirstEndNotAtTheLastOne() {
        List<MatchEvent> events = List.of(started(1), goal(2, RED), ended(3, 1, 0), goal(4, BLUE), ended(5, 1, 1));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).extracting(ScoredPoint::seq).containsExactly(2);
    }

    @Test
    void aMatchWithoutGoalsHasNoPoints() {
        List<MatchEvent> events = List.of(started(1), cancelled(2));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).isEmpty();
    }

    @Test
    void goalsOfAnotherTableAreNotPoints() {
        TableId other = new TableId(UUID.randomUUID());
        GoalScored foreign = new GoalScored(newId(), VENUE, other, MATCH, 2, NOW, RED);
        List<MatchEvent> events = List.of(started(1), foreign, goal(3, BLUE), ended(4, 0, 1));
        Match match = Match.replay(events).orElseThrow();

        assertThat(match.pointsFrom(events)).extracting(ScoredPoint::seq).containsExactly(3);
    }

    @Test
    void aPointNeedsASeqFromOneASideAndATime() {
        assertThatThrownBy(() -> new ScoredPoint(0, RED, NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ScoredPoint(1, null, NOW)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ScoredPoint(1, RED, null)).isInstanceOf(NullPointerException.class);
    }
}
