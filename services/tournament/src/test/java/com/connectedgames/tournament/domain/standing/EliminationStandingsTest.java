package com.connectedgames.tournament.domain.standing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.bracket.Bracket;
import com.connectedgames.tournament.domain.match.TournamentMatch;

import static com.connectedgames.tournament.domain.bracket.BracketFixtures.bracket;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.entrants;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.fromMatch;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.matchAt;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.play;
import static com.connectedgames.tournament.domain.bracket.BracketFixtures.thirdPlace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EliminationStandingsTest {

    private static List<Standing> standings(Bracket bracket) {
        return EliminationStandings.of(bracket, ScoringPolicy.DEFAULT);
    }

    private static Map<String, Standing> byName(List<Standing> standings) {
        return standings.stream().collect(Collectors.toMap(s -> s.entrant().id().value(), Function.identity()));
    }

    private static List<String> names(List<Standing> standings) {
        return standings.stream().map(s -> s.entrant().id().value()).toList();
    }

    private static int expectedRank(int position, int count, boolean thirdPlaceMatch) {
        if (position == 1) {
            return 1;
        }
        if (thirdPlaceMatch && count >= 4 && position == 4) {
            return 4;
        }
        return Integer.highestOneBit(position - 1) + 1;
    }

    static Stream<Arguments> sizes() {
        return IntStream.rangeClosed(2, 40).boxed()
                .flatMap(count -> Stream.of(Arguments.of(count, false), Arguments.of(count, true)));
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void theRanksOfAFullyPlayedBracketFollowTheFinalPositions(int count, boolean thirdPlaceMatch) {
        Bracket finished = play(bracket(count, thirdPlaceMatch), count * 31L);

        List<Standing> standings = standings(finished);

        assertThat(standings).hasSize(count);
        assertThat(standings.stream().map(Standing::rank).toList())
                .isEqualTo(IntStream.rangeClosed(1, count)
                        .map(position -> expectedRank(position, count, thirdPlaceMatch)).boxed().toList());
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void everyParticipantIsListedOnce(int count, boolean thirdPlaceMatch) {
        Bracket finished = play(bracket(count, thirdPlaceMatch), count);

        Set<String> listed = new HashSet<>(names(standings(finished)));

        assertThat(listed).isEqualTo(entrants(count).stream().map(e -> e.id().value()).collect(Collectors.toSet()));
        assertThat(names(standings(finished))).hasSize(count);
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void theTotalsOfAFullyPlayedBracketAddUp(int count, boolean thirdPlaceMatch) {
        Bracket finished = play(bracket(count, thirdPlaceMatch), count * 7L);
        List<Standing> standings = standings(finished);
        long playedMatches = finished.matches().stream()
                .filter(match -> match.outcome().source() != ResultSource.BYE)
                .count();

        assertThat(playedMatches).isEqualTo(count - 1 + (thirdPlaceMatch && count >= 4 ? 1 : 0));
        assertThat(standings.stream().mapToInt(Standing::played).sum()).isEqualTo(2 * playedMatches);
        assertThat(standings.stream().mapToInt(Standing::wins).sum()).isEqualTo(playedMatches);
        assertThat(standings.stream().mapToInt(Standing::losses).sum()).isEqualTo(playedMatches);
        assertThat(standings.stream().mapToInt(Standing::scoreFor).sum())
                .isEqualTo(standings.stream().mapToInt(Standing::scoreAgainst).sum());
        assertThat(standings).allMatch(s -> s.draws() == 0);
        assertThat(standings).allMatch(s -> s.points().compareTo(BigDecimal.valueOf(3L * s.wins())) == 0);
    }

    @ParameterizedTest
    @MethodSource("sizes")
    void theChampionIsFirstAndTheOnlyOneWithoutAnEliminationRound(int count, boolean thirdPlaceMatch) {
        Bracket finished = play(bracket(count, thirdPlaceMatch), count * 3L);

        List<Standing> standings = standings(finished);

        assertThat(standings.get(0).entrant()).isEqualTo(finished.champion());
        assertThat(standings.get(0).eliminatedInRound()).isNull();
        assertThat(standings.stream().skip(1)).allMatch(s -> s.eliminatedInRound() != null);
        assertThat(names(standings).get(1)).isEqualTo(finished.finalMatch().loser().id().value());
        assertThat(standings.get(1).eliminatedInRound()).isEqualTo(finished.rounds());
    }

    @Test
    void beforeTheFirstResultEveryoneSharesTheFirstRankWithNothingPlayed() {
        List<Standing> standings = standings(bracket(8, true));

        assertThat(standings).hasSize(8);
        assertThat(standings).allSatisfy(s -> {
            assertThat(s.rank()).isEqualTo(1);
            assertThat(s.played()).isZero();
            assertThat(s.wins()).isZero();
            assertThat(s.losses()).isZero();
            assertThat(s.points()).isEqualByComparingTo("0");
            assertThat(s.scoreFor()).isZero();
            assertThat(s.scoreAgainst()).isZero();
            assertThat(s.eliminatedInRound()).isNull();
        });
    }

    @Test
    void afterTheFirstRoundTheLosersAreFifthAndTheOthersAreStillFirst() {
        Bracket bracket = bracket(8, false);
        for (int slot = 0; slot < 4; slot++) {
            bracket = bracket.recordResult(matchAt(bracket, 1, slot).id(), fromMatch(Side.RED, 5, 2));
        }

        List<Standing> standings = standings(bracket);

        assertThat(standings.stream().map(Standing::rank).toList()).containsExactly(1, 1, 1, 1, 5, 5, 5, 5);
        assertThat(standings.subList(0, 4)).allMatch(s -> s.eliminatedInRound() == null && s.wins() == 1);
        assertThat(standings.subList(4, 8)).allMatch(s -> s.eliminatedInRound() == 1 && s.losses() == 1);
    }

    @Test
    void participantsWithTheSameRankAreInTheOrderOfTheBracket() {
        Bracket bracket = bracket(8, false);
        for (int slot = 0; slot < 4; slot++) {
            bracket = bracket.recordResult(matchAt(bracket, 1, slot).id(), fromMatch(Side.RED, 5, 2));
        }

        List<String> names = names(standings(bracket));

        assertThat(names.subList(0, 4)).containsExactly("p1", "p4", "p2", "p3");
        assertThat(names.subList(4, 8)).containsExactly("p8", "p5", "p7", "p6");
    }

    @Test
    void aByeIsNotAMatchPlayed() {
        Bracket bracket = bracket(3, false);
        TournamentMatch semifinal = matchAt(bracket, 1, 1);
        bracket = bracket.recordResult(semifinal.id(), fromMatch(Side.RED, 5, 1));
        bracket = bracket.recordResult(matchAt(bracket, 2, 0).id(), fromMatch(Side.RED, 5, 3));

        Map<String, Standing> standings = byName(standings(bracket));

        assertThat(standings.get("p1").played()).isEqualTo(1);
        assertThat(standings.get("p1").wins()).isEqualTo(1);
        assertThat(standings.get("p1").scoreFor()).isEqualTo(5);
        assertThat(standings.get("p1").scoreAgainst()).isEqualTo(3);
        assertThat(standings.get("p1").rank()).isEqualTo(1);
        assertThat(standings.get("p2").rank()).isEqualTo(2);
        assertThat(standings.get("p3").rank()).isEqualTo(3);
        assertThat(standings.get("p3").eliminatedInRound()).isEqualTo(1);
    }

    @Test
    void aParticipantWithAByeWhoLosesTheNextRoundIsEliminatedThere() {
        Bracket bracket = bracket(3, false);
        bracket = bracket.recordResult(matchAt(bracket, 1, 1).id(), fromMatch(Side.RED, 5, 1));
        bracket = bracket.recordResult(matchAt(bracket, 2, 0).id(), fromMatch(Side.BLUE, 1, 5));

        Map<String, Standing> standings = byName(standings(bracket));

        assertThat(standings.get("p1").eliminatedInRound()).isEqualTo(2);
        assertThat(standings.get("p1").rank()).isEqualTo(2);
        assertThat(standings.get("p1").played()).isEqualTo(1);
        assertThat(standings.get("p1").losses()).isEqualTo(1);
        assertThat(standings.get("p2").rank()).isEqualTo(1);
    }

    @Test
    void theScoresAndTheResultsAddUpForEachParticipant() {
        Bracket bracket = bracket(4, false);
        bracket = bracket.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 3));
        bracket = bracket.recordResult(matchAt(bracket, 1, 1).id(), fromMatch(Side.BLUE, 2, 5));
        bracket = bracket.recordResult(matchAt(bracket, 2, 0).id(), fromMatch(Side.RED, 5, 4));

        List<Standing> standings = standings(bracket);
        Map<String, Standing> byName = byName(standings);

        assertThat(names(standings)).containsExactly("p1", "p3", "p4", "p2");
        assertThat(standings.stream().map(Standing::rank).toList()).containsExactly(1, 2, 3, 3);

        Standing p1 = byName.get("p1");
        assertThat(p1.played()).isEqualTo(2);
        assertThat(p1.wins()).isEqualTo(2);
        assertThat(p1.losses()).isZero();
        assertThat(p1.scoreFor()).isEqualTo(10);
        assertThat(p1.scoreAgainst()).isEqualTo(7);
        assertThat(p1.points()).isEqualByComparingTo("6");
        assertThat(p1.eliminatedInRound()).isNull();

        Standing p3 = byName.get("p3");
        assertThat(p3.played()).isEqualTo(2);
        assertThat(p3.wins()).isEqualTo(1);
        assertThat(p3.losses()).isEqualTo(1);
        assertThat(p3.scoreFor()).isEqualTo(9);
        assertThat(p3.scoreAgainst()).isEqualTo(7);
        assertThat(p3.points()).isEqualByComparingTo("3");
        assertThat(p3.eliminatedInRound()).isEqualTo(2);

        Standing p4 = byName.get("p4");
        assertThat(p4.played()).isEqualTo(1);
        assertThat(p4.wins()).isZero();
        assertThat(p4.losses()).isEqualTo(1);
        assertThat(p4.scoreFor()).isEqualTo(3);
        assertThat(p4.scoreAgainst()).isEqualTo(5);
        assertThat(p4.points()).isEqualByComparingTo("0");
        assertThat(p4.eliminatedInRound()).isEqualTo(1);

        Standing p2 = byName.get("p2");
        assertThat(p2.scoreFor()).isEqualTo(2);
        assertThat(p2.scoreAgainst()).isEqualTo(5);
    }

    @Test
    void thePointsFollowTheScoringPolicy() {
        Bracket bracket = bracket(4, false);
        bracket = bracket.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 3));
        bracket = bracket.recordResult(matchAt(bracket, 1, 1).id(), fromMatch(Side.BLUE, 2, 5));
        bracket = bracket.recordResult(matchAt(bracket, 2, 0).id(), fromMatch(Side.RED, 5, 4));
        ScoringPolicy policy = new ScoringPolicy(new BigDecimal("2"), new BigDecimal("1"), new BigDecimal("0.5"),
                List.of());

        Map<String, Standing> standings = byName(EliminationStandings.of(bracket, policy));

        assertThat(standings.get("p1").points()).isEqualByComparingTo("4");
        assertThat(standings.get("p3").points()).isEqualByComparingTo("2.5");
        assertThat(standings.get("p4").points()).isEqualByComparingTo("0.5");
        assertThat(standings.get("p2").points()).isEqualByComparingTo("0.5");
    }

    @Test
    void theLosersOfTheSemifinalsShareTheThirdRankUntilTheMatchForTheThirdPlaceIsPlayed() {
        Bracket bracket = bracket(4, true);
        bracket = bracket.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 3));
        bracket = bracket.recordResult(matchAt(bracket, 1, 1).id(), fromMatch(Side.RED, 5, 3));

        List<Standing> standings = standings(bracket);

        assertThat(standings.stream().map(Standing::rank).toList()).containsExactly(1, 1, 3, 3);
        assertThat(names(standings).subList(2, 4)).containsExactly("p4", "p3");
    }

    @Test
    void theMatchForTheThirdPlaceSeparatesThirdAndFourth() {
        Bracket bracket = bracket(4, true);
        bracket = bracket.recordResult(matchAt(bracket, 1, 0).id(), fromMatch(Side.RED, 5, 3));
        bracket = bracket.recordResult(matchAt(bracket, 1, 1).id(), fromMatch(Side.RED, 5, 3));
        bracket = bracket.recordResult(matchAt(bracket, 2, 0).id(), fromMatch(Side.RED, 5, 3));
        bracket = bracket.recordResult(thirdPlace(bracket).id(), fromMatch(Side.BLUE, 4, 5));

        List<Standing> standings = standings(bracket);
        Map<String, Standing> byName = byName(standings);

        assertThat(standings.stream().map(Standing::rank).toList()).containsExactly(1, 2, 3, 4);
        assertThat(byName.get("p1").rank()).isEqualTo(1);
        assertThat(byName.get("p2").rank()).isEqualTo(2);
        assertThat(byName.get("p3").rank()).isEqualTo(3);
        assertThat(byName.get("p4").rank()).isEqualTo(4);
        assertThat(byName.get("p3").played()).isEqualTo(2);
        assertThat(byName.get("p3").wins()).isEqualTo(1);
        assertThat(byName.get("p3").eliminatedInRound()).isEqualTo(1);
        assertThat(byName.get("p4").eliminatedInRound()).isEqualTo(1);
        assertThat(byName.get("p4").played()).isEqualTo(2);
        assertThat(byName.get("p4").scoreFor()).isEqualTo(3 + 4);
        assertThat(byName.get("p4").scoreAgainst()).isEqualTo(5 + 5);
    }

    @Test
    void theFinalistWhoLostIsEliminatedInTheLastRound() {
        Bracket bracket = bracket(8, false);
        Bracket finished = play(bracket, 11);

        Standing runnerUp = standings(finished).get(1);

        assertThat(runnerUp.rank()).isEqualTo(2);
        assertThat(runnerUp.eliminatedInRound()).isEqualTo(3);
        assertThat(standings(finished).get(2).eliminatedInRound()).isEqualTo(2);
        assertThat(standings(finished).get(4).eliminatedInRound()).isEqualTo(1);
    }

    @Test
    void theOrderOfTheMatchesInTheBracketDoesNotMatter() {
        Bracket bracket = play(bracket(8, true), 5);
        List<TournamentMatch> reversed = new ArrayList<>(bracket.matches());
        Collections.reverse(reversed);

        assertThat(standings(new Bracket(reversed))).isEqualTo(standings(bracket));
    }

    @Test
    void theArgumentsAreRequired() {
        Bracket bracket = bracket(4, false);

        assertThatNullPointerException().isThrownBy(() -> EliminationStandings.of(null, ScoringPolicy.DEFAULT));
        assertThatNullPointerException().isThrownBy(() -> EliminationStandings.of(bracket, null));
    }
}
