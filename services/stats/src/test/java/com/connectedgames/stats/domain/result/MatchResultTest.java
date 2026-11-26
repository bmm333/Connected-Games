package com.connectedgames.stats.domain.result;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.domain.EndReason;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.MessageId;
import com.connectedgames.stats.domain.Side;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchResultTest {

    private static MatchResult with(int pointsToWin, Instant start, Instant end, List<SideResult> sides,
                                    List<ScoredPoint> points) {
        MatchResult base = Fixtures.result();
        return new MatchResult(base.id(), base.messageId(), base.tenantId(), base.venueId(), base.tableId(),
                base.gameTypeId(), base.tournamentMatchId(), base.mode(), pointsToWin, base.reason(),
                base.winnerSide(), start, end, sides, points);
    }

    private static final List<SideResult> TWO_SIDES =
            List.of(new SideResult(Side.RED, null, null, 5), new SideResult(Side.BLUE, null, null, 3));

    @Test
    void theWinnerWinsAndTheOtherSideLoses() {
        MatchResult result = Fixtures.result();

        assertThat(result.outcomeOf(Side.RED)).isEqualTo(Outcome.WIN);
        assertThat(result.outcomeOf(Side.BLUE)).isEqualTo(Outcome.LOSS);
    }

    @Test
    void withoutAWinnerBothSidesDraw() {
        MatchResult result = Fixtures.draw(new MatchId(UUID.randomUUID()));

        assertThat(result.outcomeOf(Side.RED)).isEqualTo(Outcome.DRAW);
        assertThat(result.outcomeOf(Side.BLUE)).isEqualTo(Outcome.DRAW);
    }

    @Test
    void theDurationIsInSecondsFromStartToEnd() {
        assertThat(Fixtures.result().durationSeconds()).isEqualTo(600);
    }

    @Test
    void aCancelledMatchDoesNotCount() {
        assertThat(Fixtures.cancelled(new MatchId(UUID.randomUUID())).counts()).isFalse();
        assertThat(Fixtures.result().counts()).isTrue();
        assertThat(Fixtures.draw(new MatchId(UUID.randomUUID())).counts()).isTrue();
    }

    @Test
    void thePointsAndTheSidesCannotBeChangedAfterwards() {
        MatchResult result = Fixtures.result();

        assertThatThrownBy(() -> result.points().add(new ScoredPoint(99, Side.RED, Fixtures.END)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.sides().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void aMatchNeedsAtLeastOnePointToWin() {
        assertThatThrownBy(() -> with(0, Fixtures.START, Fixtures.END, TWO_SIDES, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMatchCannotEndBeforeItStarts() {
        assertThatThrownBy(() -> with(5, Fixtures.END, Fixtures.START, TWO_SIDES, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMatchHasExactlyOneRedAndOneBlueSide() {
        assertThatThrownBy(() -> with(5, Fixtures.START, Fixtures.END,
                List.of(new SideResult(Side.RED, null, null, 5)), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> with(5, Fixtures.START, Fixtures.END,
                List.of(new SideResult(Side.RED, null, null, 5), new SideResult(Side.RED, null, null, 3)), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thePointsMustComeInIncreasingOrderOfSeq() {
        ScoredPoint first = new ScoredPoint(3, Side.RED, Fixtures.START);
        ScoredPoint second = new ScoredPoint(2, Side.BLUE, Fixtures.START);

        assertThatThrownBy(() -> with(5, Fixtures.START, Fixtures.END, TWO_SIDES, List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> with(5, Fixtures.START, Fixtures.END, TWO_SIDES, List.of(first, first)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thePointsCanBeFewerThanTheScore() {
        // The hub may not deliver every goal: the score is the one that counts
        MatchResult result = with(5, Fixtures.START, Fixtures.END, TWO_SIDES, Fixtures.points("RB"));

        assertThat(result.points()).hasSize(2);
        assertThat(result.sides().get(0).score()).isEqualTo(5);
    }

    @Test
    void aSideNeedsASideAndANonNegativeScoreAndNoBlankParticipant() {
        assertThatThrownBy(() -> new SideResult(null, null, null, 0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SideResult(Side.RED, null, null, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SideResult(Side.RED, " ", null, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPointNeedsASeqFromOneASideAndATime() {
        assertThatThrownBy(() -> new ScoredPoint(0, Side.RED, Fixtures.START)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ScoredPoint(1, null, Fixtures.START)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ScoredPoint(1, Side.RED, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theIdsAndTheTenantCannotBeMissing() {
        MatchResult base = Fixtures.result();

        assertThatThrownBy(() -> new MatchResult(null, base.messageId(), base.tenantId(), base.venueId(),
                base.tableId(), base.gameTypeId(), null, base.mode(), 5, EndReason.SCORE_REACHED, null,
                Fixtures.START, Fixtures.END, TWO_SIDES, List.of())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchResult(base.id(), new MessageId(UUID.randomUUID()), null, base.venueId(),
                base.tableId(), base.gameTypeId(), null, base.mode(), 5, EndReason.SCORE_REACHED, null,
                Fixtures.START, Fixtures.END, TWO_SIDES, List.of())).isInstanceOf(NullPointerException.class);
    }
}
