package com.connectedgames.tournament.domain.standing;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.match.Entrant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class StandingTest {

    private static final Entrant ADA = new Entrant(new ParticipantId("ada"), "Ada");

    private static Standing standing(int rank, int played, int wins, int draws, int losses, String points,
                                     int scoreFor, int scoreAgainst, Integer eliminatedInRound) {
        return new Standing(rank, ADA, played, wins, draws, losses, new BigDecimal(points), scoreFor, scoreAgainst,
                eliminatedInRound);
    }

    @Test
    void aStandingKeepsItsValues() {
        Standing standing = standing(3, 5, 2, 1, 2, "7", 20, 18, 2);

        assertThat(standing.rank()).isEqualTo(3);
        assertThat(standing.entrant()).isEqualTo(ADA);
        assertThat(standing.played()).isEqualTo(5);
        assertThat(standing.wins()).isEqualTo(2);
        assertThat(standing.draws()).isEqualTo(1);
        assertThat(standing.losses()).isEqualTo(2);
        assertThat(standing.points()).isEqualByComparingTo("7");
        assertThat(standing.scoreFor()).isEqualTo(20);
        assertThat(standing.scoreAgainst()).isEqualTo(18);
        assertThat(standing.eliminatedInRound()).isEqualTo(2);
    }

    @Test
    void theRoundOfEliminationIsOptional() {
        assertThat(standing(1, 0, 0, 0, 0, "0", 0, 0, null).eliminatedInRound()).isNull();
    }

    @Test
    void theRankStartsFromOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> standing(0, 0, 0, 0, 0, "0", 0, 0, null));
        assertThat(standing(1, 0, 0, 0, 0, "0", 0, 0, null).rank()).isEqualTo(1);
    }

    @Test
    void theCountsCannotBeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, -1, 0, 1, "0", 0, 0, null));
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, 1, -1, 0, "0", 0, 0, null));
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, 1, 0, -1, "0", 0, 0, null));
    }

    @Test
    void theMatchesPlayedAreTheWinsTheDrawsAndTheLosses() {
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 4, 2, 1, 2, "0", 0, 0, null))
                .withMessageContaining("played");
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 6, 2, 1, 2, "0", 0, 0, null));
    }

    @Test
    void thePointsAndTheScoresCannotBeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, 0, 0, 0, "-1", 0, 0, null));
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, 0, 0, 0, "0", -1, 0, null));
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, 0, 0, 0, "0", 0, -1, null));
    }

    @Test
    void aRoundStartsFromOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> standing(1, 0, 0, 0, 0, "0", 0, 0, 0));
        assertThat(standing(1, 0, 0, 0, 0, "0", 0, 0, 1).eliminatedInRound()).isEqualTo(1);
    }

    @Test
    void theEntrantAndThePointsAreRequired() {
        assertThatNullPointerException().isThrownBy(
                () -> new Standing(1, null, 0, 0, 0, 0, BigDecimal.ZERO, 0, 0, null));
        assertThatNullPointerException().isThrownBy(
                () -> new Standing(1, ADA, 0, 0, 0, 0, null, 0, 0, null));
    }
}
