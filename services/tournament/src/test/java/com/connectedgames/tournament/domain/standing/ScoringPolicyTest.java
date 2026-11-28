package com.connectedgames.tournament.domain.standing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ScoringPolicyTest {

    private static BigDecimal n(String value) {
        return new BigDecimal(value);
    }

    @Test
    void theDefaultIsThreeOneZeroWithTheScoreDifferenceThenTheScoreFor() {
        ScoringPolicy policy = ScoringPolicy.DEFAULT;

        assertThat(policy.win()).isEqualByComparingTo("3");
        assertThat(policy.draw()).isEqualByComparingTo("1");
        assertThat(policy.loss()).isEqualByComparingTo("0");
        assertThat(policy.tiebreakers()).containsExactly(Tiebreaker.SCORE_DIFFERENCE, Tiebreaker.SCORE_FOR);
    }

    @Test
    void thePointsAreTheSumOfEachResultTimesItsPoints() {
        ScoringPolicy policy = new ScoringPolicy(n("3"), n("1"), n("0.5"), List.of());

        assertThat(policy.pointsFor(2, 3, 4)).isEqualByComparingTo("11");
        assertThat(policy.pointsFor(0, 0, 0)).isEqualByComparingTo("0");
        assertThat(policy.pointsFor(1, 0, 0)).isEqualByComparingTo("3");
        assertThat(policy.pointsFor(0, 1, 0)).isEqualByComparingTo("1");
        assertThat(policy.pointsFor(0, 0, 1)).isEqualByComparingTo("0.5");
    }

    @Test
    void thePointsCanHaveDecimals() {
        ScoringPolicy policy = new ScoringPolicy(n("1"), n("0.5"), n("0"), List.of());

        assertThat(policy.pointsFor(1, 1, 1)).isEqualByComparingTo("1.5");
    }

    @Test
    void thePointsOfAResultCannotBeNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ScoringPolicy(n("-1"), n("1"), n("0"), List.of()));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ScoringPolicy(n("3"), n("-1"), n("0"), List.of()));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ScoringPolicy(n("3"), n("1"), n("-1"), List.of()));
    }

    @Test
    void zeroPointsAreAllowed() {
        assertThat(new ScoringPolicy(n("0"), n("0"), n("0"), List.of()).pointsFor(5, 5, 5)).isEqualByComparingTo("0");
    }

    @Test
    void aTiebreakerCannotBeListedTwice() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ScoringPolicy(n("3"), n("1"), n("0"),
                List.of(Tiebreaker.WINS, Tiebreaker.SCORE_FOR, Tiebreaker.WINS)))
                .withMessageContaining("twice");
    }

    @Test
    void theOrderOfTheTiebreakersIsKept() {
        ScoringPolicy policy = new ScoringPolicy(n("3"), n("1"), n("0"),
                List.of(Tiebreaker.BUCHHOLZ, Tiebreaker.HEAD_TO_HEAD, Tiebreaker.WINS));

        assertThat(policy.tiebreakers())
                .containsExactly(Tiebreaker.BUCHHOLZ, Tiebreaker.HEAD_TO_HEAD, Tiebreaker.WINS);
    }

    @Test
    void thePolicyKeepsItsOwnCopyOfTheTiebreakers() {
        List<Tiebreaker> tiebreakers = new ArrayList<>(List.of(Tiebreaker.WINS));
        ScoringPolicy policy = new ScoringPolicy(n("3"), n("1"), n("0"), tiebreakers);

        tiebreakers.add(Tiebreaker.SCORE_FOR);

        assertThat(policy.tiebreakers()).containsExactly(Tiebreaker.WINS);
    }

    @Test
    void everythingIsRequired() {
        assertThatNullPointerException().isThrownBy(() -> new ScoringPolicy(null, n("1"), n("0"), List.of()));
        assertThatNullPointerException().isThrownBy(() -> new ScoringPolicy(n("3"), null, n("0"), List.of()));
        assertThatNullPointerException().isThrownBy(() -> new ScoringPolicy(n("3"), n("1"), null, List.of()));
        assertThatNullPointerException().isThrownBy(() -> new ScoringPolicy(n("3"), n("1"), n("0"), null));
    }
}
