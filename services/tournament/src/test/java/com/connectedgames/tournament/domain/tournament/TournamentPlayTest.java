package com.connectedgames.tournament.domain.tournament;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.bracket.ResultNotAllowedException;
import com.connectedgames.tournament.domain.bracket.UnknownTournamentMatchException;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.Outcome;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;
import com.connectedgames.tournament.domain.standing.ScoringPolicy;
import com.connectedgames.tournament.domain.standing.Standing;
import com.connectedgames.tournament.domain.standing.Tiebreaker;

import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.NOW;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.byHand;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.details;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.draft;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.ids;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.match;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.open;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.openWith;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.playAll;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.ready;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.registrationId;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.result;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.setup;
import static com.connectedgames.tournament.domain.tournament.TournamentFixtures.started;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TournamentPlayTest {

    private static final Instant START = NOW.plusSeconds(1000);
    private static final Instant LATER = NOW.plusSeconds(5000);

    private static List<String> firstRound(Tournament tournament) {
        return tournament.bracket().matches().stream()
                .filter(match -> match.round() == 1)
                .sorted((a, b) -> Integer.compare(a.slot(), b.slot()))
                .map(match -> name(match, Side.RED) + "/" + name(match, Side.BLUE))
                .toList();
    }

    private static String name(TournamentMatch match, Side side) {
        return match.entrantOn(side) == null ? "-" : match.entrantOn(side).displayName();
    }

    @Test
    void startingCreatesTheBracketAndClosesTheRegistrations() {
        Tournament tournament = openWith(4);

        Tournament running = tournament.start(START, ids(), new Random(1));

        assertThat(running.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
        assertThat(running.startedAt()).isEqualTo(START);
        assertThat(running.completedAt()).isNull();
        assertThat(running.bracket().matches()).hasSize(3);
        assertThat(running.registrations()).isEqualTo(tournament.registrations());
        assertThat(running.details()).isEqualTo(tournament.details());
        assertThat(tournament.bracket()).isNull();
    }

    @Test
    void theMatchForTheThirdPlaceIsCreatedWhenTheFormatAsksForIt() {
        assertThat(started(4, Seeding.MANUAL, true).bracket().matches()).hasSize(4);
        assertThat(started(4, Seeding.MANUAL, true).bracket().thirdPlaceMatch()).isPresent();
        assertThat(started(4, Seeding.MANUAL, false).bracket().thirdPlaceMatch()).isEmpty();
    }

    @Test
    void theMatchesTakeTheirIdsFromTheSupplier() {
        Tournament running = started(4);

        Set<TournamentMatchId> expected = new HashSet<>();
        for (long i = 1; i <= 3; i++) {
            expected.add(new TournamentMatchId(new UUID(4, i)));
        }
        assertThat(running.bracket().matches().stream().map(TournamentMatch::id).toList())
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void theEntrantsCarryTheNameAndTheParticipantOfTheRegistration() {
        Tournament running = openWith(details(), setup(2, Seeding.MANUAL, false), 2)
                .start(START, ids(), new Random(1));

        TournamentMatch decider = running.bracket().finalMatch();
        assertThat(decider.red().displayName()).isEqualTo("Squad 1");
        assertThat(decider.red().id().value()).isEqualTo("squad-1");
        assertThat(decider.blue().displayName()).isEqualTo("Squad 2");
        assertThat(decider.blue().id().value()).isEqualTo("squad-2");
    }

    @Test
    void startingNeedsTheMinimumOfRegistrations() {
        Tournament three = openWith(details(4, null), setup(), 3);
        Tournament four = openWith(details(4, null), setup(), 4);

        assertThatThrownBy(() -> three.start(START, ids(), new Random(1)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("fewer");
        assertThat(four.start(START, ids(), new Random(1)).status()).isEqualTo(TournamentStatus.IN_PROGRESS);
    }

    @Test
    void startingTwiceChangesNothing() {
        Tournament running = started(4);

        assertThat(running.start(LATER, ids(), new Random(2))).isSameAs(running);
        Tournament completed = playAll(started(2), LATER);
        assertThat(completed.start(LATER, ids(), new Random(2))).isSameAs(completed);
    }

    @Test
    void onlyAPublishedTournamentCanStart() {
        assertThatThrownBy(() -> draft().start(START, ids(), new Random(1)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("published");
        assertThatThrownBy(() -> openWith(4).cancel().start(START, ids(), new Random(1)))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("published");
    }

    @Test
    void theArgumentsOfStartAreRequired() {
        Tournament tournament = openWith(2);

        assertThatNullPointerException().isThrownBy(() -> tournament.start(null, ids(), new Random(1)));
        assertThatNullPointerException().isThrownBy(() -> tournament.start(START, null, new Random(1)));
        assertThatNullPointerException().isThrownBy(() -> tournament.start(START, ids(), null));
    }

    @Test
    void manualSeedingPutsTheSeededFirstInTheBracket() {
        Tournament tournament = openWith(details(), setup(1, Seeding.MANUAL, false), 4)
                .seed(registrationId(3), 1)
                .seed(registrationId(1), 2);

        Tournament running = tournament.start(START, ids(), new Random(1));

        assertThat(firstRound(running)).containsExactly("Player 3/Player 4", "Player 1/Player 2");
    }

    @Test
    void manualSeedingWithoutSeedsFollowsTheOrderOfRegistration() {
        Tournament running = started(4, Seeding.MANUAL, false);

        assertThat(firstRound(running)).containsExactly("Player 1/Player 4", "Player 2/Player 3");
    }

    @Test
    void randomSeedingDrawsTheBracketWithTheGivenGenerator() {
        Tournament tournament = openWith(details(), setup(1, Seeding.RANDOM, false), 8);

        List<String> first = firstRound(tournament.start(START, ids(), new Random(7)));
        List<String> again = firstRound(tournament.start(START, ids(), new Random(7)));

        assertThat(first).isEqualTo(again);
        assertThat(first).hasSize(4);
        Set<List<String>> draws = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) {
            draws.add(firstRound(tournament.start(START, ids(), new Random(seed))));
        }
        assertThat(draws.size()).isGreaterThan(5);
    }

    @Test
    void randomSeedingIgnoresTheSeeds() {
        Tournament tournament = openWith(details(), setup(1, Seeding.RANDOM, false), 8)
                .seed(registrationId(1), 1);
        Set<String> firstOnes = new HashSet<>();

        for (long seed = 0; seed < 30; seed++) {
            firstOnes.add(firstRound(tournament.start(START, ids(), new Random(seed))).get(0));
        }

        assertThat(firstOnes.size()).isGreaterThan(3);
    }

    @Test
    void everyRegistrationIsInTheBracketOnce() {
        Tournament running = openWith(details(), setup(1, Seeding.RANDOM, true), 11)
                .start(START, ids(), new Random(3));

        List<String> inRoundOne = running.bracket().matches().stream()
                .filter(match -> match.round() == 1)
                .flatMap(match -> Stream.of(match.red(), match.blue()))
                .filter(Objects::nonNull)
                .map(Entrant::displayName).toList();

        assertThat(inRoundOne).hasSize(11).doesNotHaveDuplicates();
        assertThat(inRoundOne).containsExactlyInAnyOrderElementsOf(
                IntStream.rangeClosed(1, 11).mapToObj(i -> "Player " + i).toList());
    }

    @Test
    void aResultMakesTheWinnerAdvance() {
        Tournament running = started(4);
        TournamentMatch first = match(running, 1, 0);

        Tournament after = running.recordResult(first.id(), result(Side.BLUE, 1, 5), LATER);

        assertThat(match(after, 1, 0).status()).isEqualTo(TournamentMatchStatus.COMPLETED);
        assertThat(match(after, 2, 0).red().displayName()).isEqualTo("Player 4");
        assertThat(after.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
        assertThat(after.completedAt()).isNull();
        assertThat(after.startedAt()).isEqualTo(START);
        assertThat(running.bracket()).isNotEqualTo(after.bracket());
    }

    @Test
    void resultsAreNotAcceptedUntilItStarts() {
        TournamentMatchId anyone = new TournamentMatchId(UUID.randomUUID());

        assertThatThrownBy(() -> draft().recordResult(anyone, result(Side.RED, 5, 1), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("not in progress");
        assertThatThrownBy(() -> open().recordResult(anyone, result(Side.RED, 5, 1), NOW))
                .isInstanceOf(TournamentConflictException.class);
    }

    @Test
    void resultsAreNotAcceptedAfterACancellation() {
        Tournament cancelled = started(4).cancel();

        assertThatThrownBy(() -> cancelled.recordResult(ready(cancelled).id(), result(Side.RED, 5, 1), NOW))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("not in progress");
    }

    @Test
    void theSameResultAgainChangesNothing() {
        Tournament running = started(4);
        TournamentMatchId first = match(running, 1, 0).id();
        Outcome outcome = result(Side.RED, 5, 1);
        Tournament once = running.recordResult(first, outcome, LATER);

        assertThat(once.recordResult(first, outcome, LATER.plusSeconds(10))).isSameAs(once);
    }

    @Test
    void theSameResultAgainAfterTheEndChangesNothing() {
        Tournament running = started(2);
        TournamentMatchId decider = match(running, 1, 0).id();
        Outcome outcome = result(Side.RED, 5, 1);
        Tournament completed = running.recordResult(decider, outcome, LATER);

        assertThat(completed.recordResult(decider, outcome, LATER.plusSeconds(10))).isSameAs(completed);
        assertThat(completed.completedAt()).isEqualTo(LATER);
    }

    @Test
    void theTournamentIsCompletedWithTheFinalAndRemembersWhen() {
        Tournament running = started(2);

        Tournament completed = running.recordResult(match(running, 1, 0).id(), result(Side.RED, 5, 1), LATER);

        assertThat(completed.status()).isEqualTo(TournamentStatus.COMPLETED);
        assertThat(completed.completedAt()).isEqualTo(LATER);
        assertThat(completed.startedAt()).isEqualTo(START);
        assertThat(completed.currentRound()).isEmpty();
    }

    @Test
    void theTournamentIsCompletedWithTheLastResultOfTheBracket() {
        Tournament completed = playAll(started(7), LATER);

        assertThat(completed.status()).isEqualTo(TournamentStatus.COMPLETED);
        assertThat(completed.completedAt()).isEqualTo(LATER);
        assertThat(completed.bracket().isComplete()).isTrue();
    }

    @Test
    void withTheThirdPlaceTheTournamentWaitsForBothTheFinalAndTheConsolation() {
        Tournament running = started(4, Seeding.MANUAL, true);
        Tournament semifinals = running
                .recordResult(match(running, 1, 0).id(), result(Side.RED, 5, 1), NOW)
                .recordResult(match(running, 1, 1).id(), result(Side.RED, 5, 1), NOW);
        TournamentMatchId decider = semifinals.bracket().finalMatch().id();
        TournamentMatchId consolation = semifinals.bracket().thirdPlaceMatch().orElseThrow().id();

        Tournament finalFirst = semifinals.recordResult(decider, result(Side.RED, 5, 1), NOW.plusSeconds(1));
        assertThat(finalFirst.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
        assertThat(finalFirst.recordResult(consolation, result(Side.RED, 5, 1), LATER).status())
                .isEqualTo(TournamentStatus.COMPLETED);
        assertThat(finalFirst.recordResult(consolation, result(Side.RED, 5, 1), LATER).completedAt())
                .isEqualTo(LATER);

        Tournament consolationFirst = semifinals.recordResult(consolation, result(Side.RED, 5, 1), NOW.plusSeconds(1));
        assertThat(consolationFirst.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
        Tournament done = consolationFirst.recordResult(decider, result(Side.RED, 5, 1), LATER);
        assertThat(done.status()).isEqualTo(TournamentStatus.COMPLETED);
        assertThat(done.completedAt()).isEqualTo(LATER);
    }

    @Test
    void aResultByHandCanBeCorrectedAfterTheEndWithoutMovingTheCompletion() {
        Tournament running = started(2);
        TournamentMatchId decider = match(running, 1, 0).id();
        Tournament completed = running.recordResult(decider, byHand(Side.RED, 5, 1), LATER);

        Tournament corrected = completed.recordResult(decider, byHand(Side.BLUE, 1, 5), LATER.plusSeconds(500));

        assertThat(corrected.status()).isEqualTo(TournamentStatus.COMPLETED);
        assertThat(corrected.completedAt()).isEqualTo(LATER);
        assertThat(corrected.bracket().champion().displayName()).isEqualTo("Player 2");
    }

    @Test
    void theRefusalsOfTheBracketComeThrough() {
        Tournament running = started(4);

        assertThatThrownBy(() -> running.recordResult(new TournamentMatchId(UUID.randomUUID()),
                result(Side.RED, 5, 1), LATER)).isInstanceOf(UnknownTournamentMatchException.class);
        assertThatThrownBy(() -> running.recordResult(match(running, 2, 0).id(), result(Side.RED, 5, 1), LATER))
                .isInstanceOf(ResultNotAllowedException.class);
        assertThatIllegalArgumentException().isThrownBy(
                () -> running.recordResult(match(running, 1, 0).id(), result(null, 3, 3), LATER));
    }

    @Test
    void theTimeOfAResultIsRequired() {
        Tournament running = started(2);

        assertThatNullPointerException().isThrownBy(
                () -> running.recordResult(match(running, 1, 0).id(), result(Side.RED, 5, 1), null));
    }

    @Test
    void thereAreNoStandingsBeforeItStarts() {
        assertThat(draft().standings()).isEmpty();
        assertThat(openWith(4).standings()).isEmpty();
        assertThat(openWith(4).cancel().standings()).isEmpty();
    }

    @Test
    void whenItStartsEveryoneIsFirstWithNothingPlayed() {
        List<Standing> standings = started(4).standings();

        assertThat(standings).hasSize(4);
        assertThat(standings).allMatch(s -> s.rank() == 1 && s.played() == 0);
    }

    @Test
    void theStandingsOfACompletedTournamentRankEveryone() {
        Tournament completed = playAll(started(4, Seeding.MANUAL, true), LATER);

        assertThat(completed.standings().stream().map(Standing::rank).toList()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void theStandingsUseTheScoringOfTheTournament() {
        ScoringPolicy scoring = new ScoringPolicy(new BigDecimal("5"), BigDecimal.ONE, BigDecimal.ZERO,
                List.of(Tiebreaker.WINS));
        TournamentSetup custom = new TournamentSetup(TournamentFixtures.GAME, 1,
                new SingleEliminationSpec(Seeding.MANUAL, false), scoring);
        Tournament running = openWith(details(), custom, 2).start(START, ids(), new Random(1));
        Tournament completed = running.recordResult(match(running, 1, 0).id(), result(Side.RED, 5, 1), LATER);

        assertThat(completed.standings().get(0).points()).isEqualByComparingTo("5");
        assertThat(completed.standings().get(1).points()).isEqualByComparingTo("0");
    }

    @Test
    void theCurrentRoundIsTheFirstWithMatchesToPlay() {
        Tournament running = started(4);
        assertThat(running.currentRound()).hasValue(1);

        Tournament oneDone = running.recordResult(match(running, 1, 0).id(), result(Side.RED, 5, 1), NOW);
        assertThat(oneDone.currentRound()).hasValue(1);

        Tournament semifinals = oneDone.recordResult(match(running, 1, 1).id(), result(Side.RED, 5, 1), NOW);
        assertThat(semifinals.currentRound()).hasValue(2);
    }

    @Test
    void aByeInTheFirstRoundDoesNotMakeItSkipTheRound() {
        Tournament running = started(3);

        assertThat(running.currentRound()).hasValue(1);
    }

    @Test
    void thereIsNoCurrentRoundOutsideOfTheMatches() {
        assertThat(draft().currentRound()).isEmpty();
        assertThat(openWith(3).currentRound()).isEmpty();
        assertThat(started(3).cancel().currentRound()).isEmpty();
        assertThat(playAll(started(3), LATER).currentRound()).isEmpty();
    }
}
