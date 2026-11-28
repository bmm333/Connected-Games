package com.connectedgames.tournament.domain.bracket;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.connectedgames.tournament.domain.MatchId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.match.Advance;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;

import static com.connectedgames.tournament.domain.bracket.BracketFixtures.NOW;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.bracket;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.entrants;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.fromMatch;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.manual;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.matchAt;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.play;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.thirdPlace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BracketTest {

    private static String name(Entrant entrant) {
        return entrant == null ? null : entrant.id().value();
    }

    private static TournamentMatchId id(long value) {
        return new TournamentMatchId(new UUID(1, value));
    }

    private static TournamentMatch plain(long id, int round, int slot, Advance winnerGoesTo) {
        return new TournamentMatch(id(id), round, slot, false, null, null, winnerGoesTo, null, null);
    }

    @Test
    void theWinnerOfAnEvenSlotTakesTheRedSideOfTheNextMatch() {
        Bracket bracket = bracket(4, false);

        Bracket after = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.BLUE, 3, 5));

        TournamentMatch decider = matchAt(after, 2, 0);
        assertThat(name(decider.red())).isEqualTo("p4");
        assertThat(decider.blue()).isNull();
        assertThat(decider.status()).isEqualTo(TournamentMatchStatus.PENDING);
    }

    @Test
    void theWinnerOfAnOddSlotTakesTheBlueSideOfTheNextMatch() {
        Bracket bracket = bracket(4, false);

        Bracket after = bracket.recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));

        TournamentMatch decider = matchAt(after, 2, 0);
        assertThat(decider.red()).isNull();
        assertThat(name(decider.blue())).isEqualTo("p2");
    }

    @Test
    void theNextMatchIsReadyWhenBothFeedersAreDone() {
        Bracket bracket = bracket(4, false);
        Bracket after = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.BLUE, 1, 5));

        TournamentMatch decider = matchAt(after, 2, 0);
        assertThat(name(decider.red())).isEqualTo("p1");
        assertThat(name(decider.blue())).isEqualTo("p3");
        assertThat(decider.status()).isEqualTo(TournamentMatchStatus.READY);
    }

    @Test
    void theRecordedMatchKeepsItsResultAndIsCompleted() {
        Bracket bracket = bracket(4, false);
        Outcome outcome = manual(Side.RED, 5, 1);

        Bracket after = bracket.recordResult(matchAt(bracket, 1, 0).id(), outcome);

        assertThat(matchAt(after, 1, 0).outcome()).isEqualTo(outcome);
        assertThat(matchAt(after, 1, 0).status()).isEqualTo(TournamentMatchStatus.COMPLETED);
    }

    @Test
    void theLosersOfTheSemifinalsGoToTheMatchForTheThirdPlace() {
        Bracket bracket = bracket(4, true);

        Bracket after = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.BLUE, 1, 5));

        TournamentMatch consolation = thirdPlace(after);
        assertThat(name(consolation.red())).isEqualTo("p4");
        assertThat(name(consolation.blue())).isEqualTo("p2");
        assertThat(consolation.status()).isEqualTo(TournamentMatchStatus.READY);
    }

    @Test
    void theLoserGoesNowhereWithoutAMatchForTheThirdPlace() {
        Bracket bracket = bracket(4, false);

        Bracket after = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1));

        assertThat(after.matches().stream()
                .filter(match -> match.round() == 2)
                .map(match -> name(match.red()) + "/" + name(match.blue())))
                .containsExactly("p1/null");
    }

    @Test
    void theOriginalBracketIsNotChanged() {
        Bracket bracket = bracket(4, true);
        List<TournamentMatch> before = bracket.matches();

        Bracket after = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1));

        assertThat(after).isNotEqualTo(bracket);
        assertThat(bracket.matches()).isEqualTo(before);
        assertThat(matchAt(bracket, 1, 0).outcome()).isNull();
        assertThat(matchAt(bracket, 2, 0).red()).isNull();
    }

    @Test
    void anUnknownMatchIsRefused() {
        Bracket bracket = bracket(4, false);
        TournamentMatchId unknown = new TournamentMatchId(UUID.randomUUID());

        assertThatThrownBy(() -> bracket.recordResult(unknown, manual(Side.RED, 5, 1)))
                .isInstanceOf(UnknownTournamentMatchException.class)
                .hasMessageContaining(unknown.value().toString());
    }

    @Test
    void aMatchWaitingForItsParticipantsIsRefused() {
        Bracket bracket = bracket(4, false);
        Bracket half = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1));

        assertThatThrownBy(() -> bracket.recordResult(matchAt(bracket, 2, 0).id(), manual(Side.RED, 5, 1)))
                .isInstanceOf(ResultNotAllowedException.class);
        assertThatThrownBy(() -> half.recordResult(matchAt(half, 2, 0).id(), manual(Side.RED, 5, 1)))
                .isInstanceOf(ResultNotAllowedException.class);
    }

    @Test
    void aDrawIsRefused() {
        Bracket bracket = bracket(4, false);
        TournamentMatchId id = matchAt(bracket, 1, 0).id();

        assertThatIllegalArgumentException().isThrownBy(() -> bracket.recordResult(id, manual(null, 3, 3)))
                .withMessageContaining("draw");
    }

    private static final Outcome A_BYE = new Outcome(Side.RED, 0, 0, ResultSource.BYE, null, NOW);

    @Test
    void aByeCannotBeRecordedOnAReadyMatch() {
        Bracket bracket = bracket(4, false);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> bracket.recordResult(matchAt(bracket, 1, 0).id(), A_BYE))
                .withMessageContaining("given by the bracket");
    }

    @Test
    void aByeCannotBeRecordedOnAWaitingMatch() {
        Bracket bracket = bracket(4, false);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> bracket.recordResult(matchAt(bracket, 2, 0).id(), A_BYE))
                .withMessageContaining("given by the bracket");
    }

    @Test
    void aByeCannotBeRecordedAgainOnTheMatchOfAByeEither() {
        Bracket bracket = bracket(3, false);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> bracket.recordResult(matchAt(bracket, 1, 0).id(), A_BYE))
                .withMessageContaining("given by the bracket");
    }

    @Test
    void aByeCannotBeChanged() {
        Bracket bracket = bracket(3, false);
        TournamentMatch bye = matchAt(bracket, 1, 0);
        assertThat(bye.outcome().source()).isEqualTo(ResultSource.BYE);

        assertThatThrownBy(() -> bracket.recordResult(bye.id(), manual(Side.RED, 5, 1)))
                .isInstanceOf(ResultNotAllowedException.class)
                .hasMessageContaining("bye");
    }

    @Test
    void aByeCannotBeChangedEvenWithTheSameWinnerAndScore() {
        Bracket bracket = bracket(3, false);
        TournamentMatch bye = matchAt(bracket, 1, 0);

        assertThatThrownBy(() -> bracket.recordResult(bye.id(), manual(Side.RED, 0, 0)))
                .isInstanceOf(ResultNotAllowedException.class)
                .hasMessageContaining("bye");
    }

    @Test
    void nullArgumentsAreRefused() {
        Bracket bracket = bracket(4, false);

        assertThatNullPointerException().isThrownBy(() -> bracket.recordResult(null, manual(Side.RED, 5, 1)));
        assertThatNullPointerException().isThrownBy(() -> bracket.recordResult(matchAt(bracket, 1, 0).id(), null));
    }

    @Test
    void theSameResultOfAMatchAgainChangesNothing() {
        Bracket bracket = bracket(4, true);
        Outcome outcome = fromMatch(Side.RED, 5, 1);
        Bracket once = bracket.recordResult(matchAt(bracket, 1, 0).id(), outcome);

        Bracket twice = once.recordResult(matchAt(bracket, 1, 0).id(), outcome);

        assertThat(twice).isSameAs(once);
    }

    @Test
    void theSameResultByHandAgainChangesNothingEvenWithAnotherClock() {
        Bracket bracket = bracket(4, true);
        Bracket once = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1));
        Outcome later = new Outcome(Side.RED, 5, 1, ResultSource.MANUAL, null, NOW.plus(Duration.ofHours(1)));

        Bracket twice = once.recordResult(matchAt(bracket, 1, 0).id(), later);

        assertThat(twice).isSameAs(once);
        assertThat(matchAt(twice, 1, 0).outcome().completedAt()).isEqualTo(NOW);
    }

    @Test
    void theSameResultStaysAcceptedAfterTheNextMatchIsDone() {
        Bracket bracket = bracket(4, false);
        Outcome outcome = fromMatch(Side.RED, 5, 1);
        Bracket played = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), outcome)
                .recordResult(matchAt(bracket, 1, 1).id(), fromMatch(Side.RED, 5, 1));
        Bracket decided = played.recordResult(matchAt(played, 2, 0).id(), fromMatch(Side.BLUE, 1, 5));

        assertThat(decided.recordResult(matchAt(bracket, 1, 0).id(), outcome)).isSameAs(decided);
    }

    @Test
    void theResultOfAMatchCannotBeReplacedByHand() {
        Bracket bracket = bracket(4, false);
        Bracket played = bracket.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 1));

        assertThatThrownBy(() -> played.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.BLUE, 1, 5)))
                .isInstanceOf(ResultNotAllowedException.class)
                .hasMessageContaining("cannot be replaced");
    }

    @Test
    void theResultOfAMatchCannotBeReplacedByAnotherMatch() {
        Bracket bracket = bracket(4, false);
        Bracket played = bracket.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 1));

        assertThatThrownBy(() -> played.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 1)))
                .isInstanceOf(ResultNotAllowedException.class);
    }

    static Stream<Arguments> otherResultsOfTheSameMatch() {
        return Stream.of(
                Arguments.of(Side.BLUE, 5, 1),
                Arguments.of(Side.RED, 6, 1),
                Arguments.of(Side.RED, 5, 2));
    }

    @ParameterizedTest
    @MethodSource("otherResultsOfTheSameMatch")
    void aResultOfAMatchWithTheSameMatchIdButAnotherOutcomeIsRefused(Side winner, int red, int blue) {
        Bracket bracket = bracket(4, false);
        MatchId matchId = new MatchId(UUID.randomUUID());
        Bracket played = bracket.recordResult(matchAt(bracket, 1, 0).id(),
                new Outcome(Side.RED, 5, 1, ResultSource.MATCH, matchId, NOW));

        assertThatThrownBy(() -> played.recordResult(matchAt(bracket, 1, 0).id(),
                new Outcome(winner, red, blue, ResultSource.MATCH, matchId, NOW)))
                .isInstanceOf(ResultNotAllowedException.class);
    }

    @Test
    void theScoresOfAResultByHandCanBeCorrected() {
        Bracket bracket = bracket(4, false);
        Bracket first = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1));

        Bracket corrected = first.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 3));

        assertThat(matchAt(corrected, 1, 0).outcome().blueScore()).isEqualTo(3);
        assertThat(name(matchAt(corrected, 2, 0).red())).isEqualTo("p1");
    }

    @Test
    void theScoresCanBeCorrectedEvenWhenTheNextMatchIsDone() {
        Bracket bracket = bracket(4, false);
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));
        Bracket finished = decided.recordResult(matchAt(decided, 2, 0).id(), manual(Side.RED, 5, 2));

        Bracket corrected = finished.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 4));

        assertThat(matchAt(corrected, 1, 0).outcome().blueScore()).isEqualTo(4);
        assertThat(matchAt(corrected, 2, 0).outcome()).isEqualTo(matchAt(finished, 2, 0).outcome());
    }

    @Test
    void correctingTheWinnerMovesBothParticipantsOfTheNextMatches() {
        Bracket bracket = bracket(4, true);
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));

        Bracket corrected = decided.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.BLUE, 1, 5));

        assertThat(name(matchAt(corrected, 2, 0).red())).isEqualTo("p4");
        assertThat(name(matchAt(corrected, 2, 0).blue())).isEqualTo("p2");
        assertThat(name(thirdPlace(corrected).red())).isEqualTo("p1");
        assertThat(name(thirdPlace(corrected).blue())).isEqualTo("p3");
    }

    @Test
    void correctingTheWinnerIsRefusedWhenTheNextMatchIsDone() {
        Bracket bracket = bracket(4, false);
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));
        Bracket finished = decided.recordResult(matchAt(decided, 2, 0).id(), manual(Side.RED, 5, 2));

        assertThatThrownBy(() -> finished.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.BLUE, 1, 5)))
                .isInstanceOf(ResultNotAllowedException.class)
                .hasMessageContaining("next match");
        assertThat(name(matchAt(finished, 1, 0).winner())).isEqualTo("p1");
    }

    @Test
    void correctingTheWinnerIsRefusedWhenTheMatchForTheThirdPlaceIsDone() {
        Bracket bracket = bracket(4, true);
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));
        Bracket consoled = decided.recordResult(thirdPlace(decided).id(), manual(Side.RED, 5, 2));

        assertThatThrownBy(() -> consoled.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.BLUE, 1, 5)))
                .isInstanceOf(ResultNotAllowedException.class);
        assertThat(matchAt(consoled, 2, 0).status()).isEqualTo(TournamentMatchStatus.READY);
        assertThat(name(matchAt(consoled, 2, 0).red())).isEqualTo("p1");
    }

    @Test
    void theResultOfAMatchReplacesOneByHand() {
        Bracket bracket = bracket(4, false);
        Bracket byHand = bracket.recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1));
        Outcome fromHub = fromMatch(Side.BLUE, 2, 5);

        Bracket replaced = byHand.recordResult(matchAt(bracket, 1, 0).id(), fromHub);

        assertThat(matchAt(replaced, 1, 0).outcome()).isEqualTo(fromHub);
        assertThat(name(matchAt(replaced, 2, 0).red())).isEqualTo("p4");
    }

    @Test
    void theResultOfAMatchCannotReplaceOneByHandWhenTheNextMatchIsDone() {
        Bracket bracket = bracket(4, false);
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));
        Bracket finished = decided.recordResult(matchAt(decided, 2, 0).id(), manual(Side.RED, 5, 2));

        assertThatThrownBy(() -> finished.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.BLUE, 1, 5)))
                .isInstanceOf(ResultNotAllowedException.class);
    }

    @Test
    void theChampionIsKnownWhenTheFinalIsDone() {
        Bracket bracket = bracket(4, false);
        assertThat(bracket.champion()).isNull();
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.BLUE, 1, 5));

        assertThat(decided.champion()).isNull();
        Bracket finished = decided.recordResult(matchAt(decided, 2, 0).id(), manual(Side.BLUE, 1, 5));

        assertThat(name(finished.champion())).isEqualTo("p3");
    }

    @Test
    void withoutAMatchForTheThirdPlaceTheBracketIsCompleteWithTheFinal() {
        Bracket bracket = bracket(4, false);
        assertThat(bracket.isComplete()).isFalse();

        Bracket finished = play(bracket, 1);

        assertThat(finished.isComplete()).isTrue();
    }

    @Test
    void withAMatchForTheThirdPlaceTheBracketIsCompleteOnlyWhenItIsDoneToo() {
        Bracket bracket = bracket(4, true);
        Bracket decided = bracket
                .recordResult(matchAt(bracket, 1, 0).id(), manual(Side.RED, 5, 1))
                .recordResult(matchAt(bracket, 1, 1).id(), manual(Side.RED, 5, 1));

        Bracket finalPlayed = decided.recordResult(matchAt(decided, 2, 0).id(), manual(Side.RED, 5, 1));
        assertThat(finalPlayed.isComplete()).isFalse();

        Bracket consolationPlayed = decided.recordResult(thirdPlace(decided).id(), manual(Side.RED, 5, 1));
        assertThat(consolationPlayed.isComplete()).isFalse();

        assertThat(finalPlayed.recordResult(thirdPlace(finalPlayed).id(), manual(Side.RED, 5, 1)).isComplete())
                .isTrue();
    }

    @Test
    void aBracketKnowsItsRoundsFinalAndThirdPlace() {
        Bracket bracket = bracket(8, true);

        assertThat(bracket.rounds()).isEqualTo(3);
        assertThat(bracket.finalMatch()).isEqualTo(matchAt(bracket, 3, 0));
        assertThat(bracket.thirdPlaceMatch()).contains(thirdPlace(bracket));
        assertThat(bracket(8, false).thirdPlaceMatch()).isEmpty();
        assertThat(bracket(2, true).thirdPlaceMatch()).isEmpty();
        assertThat(bracket(2, true).rounds()).isEqualTo(1);
    }

    @Test
    void theFinalAndTheThirdPlaceAreFoundWhateverTheOrderOfTheMatches() {
        Bracket bracket = bracket(4, true);
        List<TournamentMatch> reversed = new ArrayList<>(bracket.matches());
        Collections.reverse(reversed);

        Bracket shuffled = new Bracket(reversed);

        assertThat(shuffled.finalMatch()).isEqualTo(matchAt(bracket, 2, 0));
        assertThat(shuffled.thirdPlaceMatch()).contains(thirdPlace(bracket));
        assertThat(shuffled.rounds()).isEqualTo(2);
    }

    @Test
    void aMatchIsFoundById() {
        Bracket bracket = bracket(4, false);
        TournamentMatch match = matchAt(bracket, 1, 1);

        assertThat(bracket.find(match.id())).contains(match);
        assertThat(bracket.find(new TournamentMatchId(UUID.randomUUID()))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 8, 13, 16, 31})
    void everyMatchOfAFullyPlayedBracketIsCompleted(int count) {
        Bracket finished = play(bracket(count, true), 7);

        assertThat(finished.isComplete()).isTrue();
        assertThat(finished.matches()).allMatch(match -> match.status() == TournamentMatchStatus.COMPLETED);
        assertThat(finished.champion()).isIn(entrants(count));
    }

    @Test
    void aBracketKeepsItsOwnCopyOfTheMatches() {
        List<TournamentMatch> matches = new ArrayList<>(bracket(4, false).matches());
        Bracket bracket = new Bracket(matches);

        matches.clear();

        assertThat(bracket.matches()).hasSize(3);
    }

    @Test
    void aBracketNeedsMatches() {
        assertThatNullPointerException().isThrownBy(() -> new Bracket(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of()));
    }

    @Test
    void aMatchCannotBeInTheBracketTwice() {
        TournamentMatch match = plain(1, 1, 0, null);

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(match, match)))
                .withMessageContaining("twice");
    }

    @Test
    void aMatchCannotAdvanceToAMatchOutsideTheBracket() {
        TournamentMatch lone = plain(1, 1, 0, new Advance(id(99), Side.RED));

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(lone)))
                .withMessageContaining("not in the bracket");
    }

    @Test
    void aMatchCannotAdvanceToItself() {
        TournamentMatch loop = plain(1, 1, 0, new Advance(id(1), Side.RED));

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(loop)))
                .withMessageContaining("itself");
    }

    @Test
    void twoMatchesCannotAdvanceToTheSameSide() {
        TournamentMatch first = plain(1, 1, 0, new Advance(id(3), Side.RED));
        TournamentMatch second = plain(2, 1, 1, new Advance(id(3), Side.RED));
        TournamentMatch decider = plain(3, 2, 0, null);

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(first, second, decider)))
                .withMessageContaining("same side");
    }

    @Test
    void twoMatchesCanAdvanceToTheTwoSidesOfTheSameMatch() {
        TournamentMatch first = plain(1, 1, 0, new Advance(id(3), Side.RED));
        TournamentMatch second = plain(2, 1, 1, new Advance(id(3), Side.BLUE));
        TournamentMatch decider = plain(3, 2, 0, null);

        assertThat(new Bracket(List.of(first, second, decider)).matches()).hasSize(3);
    }

    @Test
    void aBracketHasExactlyOneFinal() {
        TournamentMatch first = plain(1, 1, 0, null);
        TournamentMatch second = plain(2, 1, 1, null);

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(first, second)))
                .withMessageContaining("one final");
        TournamentMatch consolation = new TournamentMatch(id(3), 1, 2, true, null, null, null, null, null);
        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(consolation)))
                .withMessageContaining("one final");
    }

    @Test
    void aBracketHasAtMostOneMatchForTheThirdPlace() {
        TournamentMatch decider = plain(1, 2, 0, null);
        TournamentMatch one = new TournamentMatch(id(2), 2, 1, true, null, null, null, null, null);
        TournamentMatch two = new TournamentMatch(id(3), 2, 2, true, null, null, null, null, null);

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(decider, one, two)))
                .withMessageContaining("third place");
    }

    @Test
    void aBracketCannotHoldADrawnMatch() {
        Bracket generated = bracket(2, false);
        TournamentMatch pair = generated.matches().get(0);
        TournamentMatch drawn = pair.withOutcome(manual(null, 3, 3));

        assertThatIllegalArgumentException().isThrownBy(() -> new Bracket(List.of(drawn)))
                .withMessageContaining("draws");
    }
}
