package com.connectedgames.match.domain.match;

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

class MatchTest {

    private static final VenueId VENUE = new VenueId(UUID.randomUUID());
    private static final TableId TABLE = new TableId(UUID.randomUUID());
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());
    private static final Instant NOW = Instant.parse("2026-11-15T20:00:00Z");

    private static EventId newId() { return new EventId(UUID.randomUUID()); }

    private static MatchStarted started(int seq) {
        return new MatchStarted(newId(), VENUE, TABLE, MATCH, seq, NOW, GameMode.INDIVIDUAL, 5,
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
}
