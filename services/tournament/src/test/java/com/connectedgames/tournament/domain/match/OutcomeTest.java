package com.connectedgames.tournament.domain.match;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.MatchId;
import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class OutcomeTest {

    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");
    private static final MatchId MATCH = new MatchId(UUID.randomUUID());

    @Test
    void aWinIsNotADraw() {
        assertThat(new Outcome(Side.RED, 5, 3, ResultSource.MATCH, MATCH, NOW).isDraw()).isFalse();
    }

    @Test
    void noWinnerIsADraw() {
        assertThat(new Outcome(null, 2, 2, ResultSource.MANUAL, null, NOW).isDraw()).isTrue();
    }

    @Test
    void aScoreCannotBeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, -1, 3, ResultSource.MATCH, MATCH, NOW));
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, 5, -1, ResultSource.MATCH, MATCH, NOW));
    }

    @Test
    void aResultFromAMatchKeepsTheMatchId() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, 5, 3, ResultSource.MATCH, null, NOW));
    }

    @Test
    void onlyAResultFromAMatchHasAMatchId() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, 5, 3, ResultSource.MANUAL, MATCH, NOW));
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, 0, 0, ResultSource.BYE, MATCH, NOW));
    }

    @Test
    void aByeHasAWinnerAndNoScore() {
        assertThat(new Outcome(Side.BLUE, 0, 0, ResultSource.BYE, null, NOW).winnerSide()).isEqualTo(Side.BLUE);
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(null, 0, 0, ResultSource.BYE, null, NOW));
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, 1, 0, ResultSource.BYE, null, NOW));
        assertThatIllegalArgumentException().isThrownBy(() -> new Outcome(Side.RED, 0, 1, ResultSource.BYE, null, NOW));
    }

    @Test
    void needsTheSourceAndTheTime() {
        assertThatNullPointerException().isThrownBy(() -> new Outcome(Side.RED, 5, 3, null, null, NOW));
        assertThatNullPointerException().isThrownBy(() -> new Outcome(Side.RED, 5, 3, ResultSource.MANUAL, null, null));
    }

    @Test
    void sidesKnowTheirOpposite() {
        assertThat(Side.RED.opposite()).isEqualTo(Side.BLUE);
        assertThat(Side.BLUE.opposite()).isEqualTo(Side.RED);
    }

    @Test
    void participantsAndEntrantsHaveNoBlankNames() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ParticipantId(" "));
        assertThatNullPointerException().isThrownBy(() -> new ParticipantId(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new Entrant(new ParticipantId("ada"), " "));
        assertThatNullPointerException().isThrownBy(() -> new Entrant(null, "Ada"));
    }
}
