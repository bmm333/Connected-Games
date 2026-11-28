package com.connectedgames.tournament.domain.match;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.MatchId;
import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TournamentMatchTest {

    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");
    private static final Entrant ADA = new Entrant(new ParticipantId("ada"), "Ada");
    private static final Entrant BRUNO = new Entrant(new ParticipantId("bruno"), "Bruno");

    private static TournamentMatch match(Entrant red, Entrant blue, Outcome outcome) {
        return new TournamentMatch(new TournamentMatchId(UUID.randomUUID()), 1, 0, false, red, blue, null, null,
                outcome);
    }

    private static Outcome played(Side winner, int red, int blue) {
        return new Outcome(winner, red, blue, ResultSource.MATCH, new MatchId(UUID.randomUUID()), NOW);
    }

    @Test
    void waitsUntilBothEntrantsAreKnown() {
        assertThat(match(null, null, null).status()).isEqualTo(TournamentMatchStatus.PENDING);
        assertThat(match(ADA, null, null).status()).isEqualTo(TournamentMatchStatus.PENDING);
        assertThat(match(null, BRUNO, null).status()).isEqualTo(TournamentMatchStatus.PENDING);
    }

    @Test
    void isReadyWhenBothEntrantsAreKnown() {
        assertThat(match(ADA, BRUNO, null).status()).isEqualTo(TournamentMatchStatus.READY);
    }

    @Test
    void isCompletedWithAnOutcome() {
        assertThat(match(ADA, BRUNO, played(Side.RED, 5, 3)).status()).isEqualTo(TournamentMatchStatus.COMPLETED);
    }

    @Test
    void knowsTheWinnerAndTheLoser() {
        TournamentMatch redWon = match(ADA, BRUNO, played(Side.RED, 5, 3));
        TournamentMatch blueWon = match(ADA, BRUNO, played(Side.BLUE, 1, 5));

        assertThat(redWon.winner()).isEqualTo(ADA);
        assertThat(redWon.loser()).isEqualTo(BRUNO);
        assertThat(blueWon.winner()).isEqualTo(BRUNO);
        assertThat(blueWon.loser()).isEqualTo(ADA);
    }

    @Test
    void aDrawHasNoWinnerAndNoLoser() {
        TournamentMatch draw = match(ADA, BRUNO, played(null, 2, 2));

        assertThat(draw.winner()).isNull();
        assertThat(draw.loser()).isNull();
    }

    @Test
    void notPlayedYetHasNoWinnerAndNoLoser() {
        TournamentMatch open = match(ADA, BRUNO, null);

        assertThat(open.winner()).isNull();
        assertThat(open.loser()).isNull();
    }

    @Test
    void aByeHasAWinnerButNoLoser() {
        TournamentMatch bye = match(ADA, null, new Outcome(Side.RED, 0, 0, ResultSource.BYE, null, NOW));

        assertThat(bye.winner()).isEqualTo(ADA);
        assertThat(bye.loser()).isNull();
        assertThat(bye.status()).isEqualTo(TournamentMatchStatus.COMPLETED);
    }

    @Test
    void entrantOnReadsTheSide() {
        TournamentMatch match = match(ADA, BRUNO, null);

        assertThat(match.entrantOn(Side.RED)).isEqualTo(ADA);
        assertThat(match.entrantOn(Side.BLUE)).isEqualTo(BRUNO);
    }

    @Test
    void aParticipantCannotPlayAgainstItself() {
        assertThatIllegalArgumentException().isThrownBy(() -> match(ADA, new Entrant(ADA.id(), "Ada twin"), null));
    }

    @Test
    void aResultNeedsBothEntrantsUnlessItIsABye() {
        assertThatIllegalArgumentException().isThrownBy(() -> match(ADA, null, played(Side.RED, 5, 3)));
        assertThatIllegalArgumentException().isThrownBy(() -> match(null, null, played(null, 0, 0)));
    }

    @Test
    void aByeHasExactlyOneEntrant() {
        Outcome bye = new Outcome(Side.RED, 0, 0, ResultSource.BYE, null, NOW);

        assertThatIllegalArgumentException().isThrownBy(() -> match(ADA, BRUNO, bye));
        assertThatIllegalArgumentException().isThrownBy(() -> match(null, null, bye));
    }

    @Test
    void theWinnerSideMustHaveAnEntrant() {
        Outcome byeForBlue = new Outcome(Side.BLUE, 0, 0, ResultSource.BYE, null, NOW);

        assertThatIllegalArgumentException().isThrownBy(() -> match(ADA, null, byeForBlue));
    }

    @Test
    void roundAndSlotHaveABound() {
        TournamentMatchId id = new TournamentMatchId(UUID.randomUUID());

        assertThatIllegalArgumentException().isThrownBy(
                () -> new TournamentMatch(id, 0, 0, false, null, null, null, null, null));
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TournamentMatch(id, 1, -1, false, null, null, null, null, null));
        assertThatNullPointerException().isThrownBy(
                () -> new TournamentMatch(null, 1, 0, false, null, null, null, null, null));
    }

    @Test
    void withAnOutcomeTheMatchIsCompletedAndTheOriginalIsUntouched() {
        TournamentMatch open = match(ADA, BRUNO, null);
        Outcome outcome = played(Side.BLUE, 2, 5);

        TournamentMatch done = open.withOutcome(outcome);

        assertThat(done.outcome()).isEqualTo(outcome);
        assertThat(done.id()).isEqualTo(open.id());
        assertThat(done.red()).isEqualTo(ADA);
        assertThat(done.blue()).isEqualTo(BRUNO);
        assertThat(open.outcome()).isNull();
    }

    @Test
    void withAnOutcomeTheRulesOfTheMatchStillApply() {
        TournamentMatch waiting = match(ADA, null, null);

        assertThatIllegalArgumentException().isThrownBy(() -> waiting.withOutcome(played(Side.RED, 5, 1)));
    }

    @Test
    void withAnEntrantOnTheRedSideOnlyTheRedSideChanges() {
        TournamentMatch open = match(ADA, BRUNO, null);
        Entrant carla = new Entrant(new ParticipantId("carla"), "Carla");

        TournamentMatch changed = open.withEntrant(Side.RED, carla);

        assertThat(changed.red()).isEqualTo(carla);
        assertThat(changed.blue()).isEqualTo(BRUNO);
        assertThat(open.red()).isEqualTo(ADA);
    }

    @Test
    void withAnEntrantOnTheBlueSideOnlyTheBlueSideChanges() {
        TournamentMatch open = match(ADA, BRUNO, null);
        Entrant carla = new Entrant(new ParticipantId("carla"), "Carla");

        TournamentMatch changed = open.withEntrant(Side.BLUE, carla);

        assertThat(changed.red()).isEqualTo(ADA);
        assertThat(changed.blue()).isEqualTo(carla);
    }

    @Test
    void anEntrantCanFillAnEmptySideAndTheMatchBecomesReady() {
        TournamentMatch waiting = match(ADA, null, null);

        TournamentMatch ready = waiting.withEntrant(Side.BLUE, BRUNO);

        assertThat(ready.status()).isEqualTo(TournamentMatchStatus.READY);
    }

    @Test
    void withAnEntrantTheRulesOfTheMatchStillApply() {
        TournamentMatch open = match(ADA, BRUNO, null);

        assertThatIllegalArgumentException().isThrownBy(() -> open.withEntrant(Side.BLUE, ADA));
    }

    @Test
    void theLinksAndTheSlotSurviveAChange() {
        Advance next = new Advance(new TournamentMatchId(UUID.randomUUID()), Side.BLUE);
        Advance consolation = new Advance(new TournamentMatchId(UUID.randomUUID()), Side.RED);
        TournamentMatch original = new TournamentMatch(new TournamentMatchId(UUID.randomUUID()), 2, 3, false, ADA,
                BRUNO, next, consolation, null);

        TournamentMatch withOutcome = original.withOutcome(played(Side.RED, 5, 1));
        TournamentMatch withEntrant = original.withEntrant(Side.RED, new Entrant(new ParticipantId("carla"), "Carla"));

        for (TournamentMatch changed : new TournamentMatch[] {withOutcome, withEntrant}) {
            assertThat(changed.id()).isEqualTo(original.id());
            assertThat(changed.round()).isEqualTo(2);
            assertThat(changed.slot()).isEqualTo(3);
            assertThat(changed.thirdPlace()).isFalse();
            assertThat(changed.winnerGoesTo()).isEqualTo(next);
            assertThat(changed.loserGoesTo()).isEqualTo(consolation);
        }
    }
}
