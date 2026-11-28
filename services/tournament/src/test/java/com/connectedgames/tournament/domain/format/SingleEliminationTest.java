package com.connectedgames.tournament.domain.format;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.ResultSource;
import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.TournamentMatch;
import com.connectedgames.tournament.domain.match.TournamentMatchStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SingleEliminationTest {

    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    private final SingleElimination plain = new SingleElimination(false);
    private final SingleElimination withThirdPlace = new SingleElimination(true);

    private static List<Entrant> entrants(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new Entrant(new ParticipantId("p" + i), "Player " + i)).toList();
    }

    private static Supplier<TournamentMatchId> randomIds() {
        return () -> new TournamentMatchId(UUID.randomUUID());
    }

    private static List<TournamentMatch> create(SingleElimination format, int count) {
        return format.createMatches(entrants(count), NOW, randomIds());
    }

    private static String name(Entrant entrant) {
        return entrant == null ? "-" : entrant.id().value();
    }

    private static List<String> round(List<TournamentMatch> matches, int round) {
        return matches.stream().filter(m -> m.round() == round && !m.thirdPlace())
                .sorted((a, b) -> Integer.compare(a.slot(), b.slot()))
                .map(m -> name(m.red()) + "/" + name(m.blue())).toList();
    }

    private static int sizeFor(int count) {
        int size = 1;
        while (size < count) {
            size *= 2;
        }
        return size;
    }

    private static Map<TournamentMatchId, TournamentMatch> byId(List<TournamentMatch> matches) {
        return matches.stream().collect(Collectors.toMap(TournamentMatch::id, m -> m));
    }

    @Test
    void twoEntrantsPlayOnlyTheFinal() {
        List<TournamentMatch> matches = create(plain, 2);

        assertThat(matches).hasSize(1);
        TournamentMatch final_ = matches.get(0);
        assertThat(final_.round()).isEqualTo(1);
        assertThat(name(final_.red())).isEqualTo("p1");
        assertThat(name(final_.blue())).isEqualTo("p2");
        assertThat(final_.status()).isEqualTo(TournamentMatchStatus.READY);
        assertThat(final_.winnerGoesTo()).isNull();
    }

    @Test
    void fourEntrantsMakeTwoSemifinalsAndAFinal() {
        List<TournamentMatch> matches = create(plain, 4);

        assertThat(round(matches, 1)).containsExactly("p1/p4", "p2/p3");
        assertThat(round(matches, 2)).containsExactly("-/-");
        assertThat(matches).extracting(TournamentMatch::status).containsExactly(
                TournamentMatchStatus.READY, TournamentMatchStatus.READY, TournamentMatchStatus.PENDING);
    }

    @Test
    void theSeedsAreSetSoThatTheBestMeetTheWorst() {
        assertThat(round(create(plain, 8), 1)).containsExactly("p1/p8", "p4/p5", "p2/p7", "p3/p6");
        assertThat(round(create(plain, 16), 1)).containsExactly(
                "p1/p16", "p8/p9", "p4/p13", "p5/p12", "p2/p15", "p7/p10", "p3/p14", "p6/p11");
    }

    @Test
    void theTwoBestSeedsCanOnlyMeetInTheFinal() {
        List<TournamentMatch> matches = create(plain, 16);
        Map<TournamentMatchId, TournamentMatch> byId = byId(matches);

        List<TournamentMatchId> first = pathOf(matches, byId, "p1");
        List<TournamentMatchId> second = pathOf(matches, byId, "p2");

        Set<TournamentMatchId> common = new HashSet<>(first);
        common.retainAll(second);
        assertThat(common).hasSize(1);
        TournamentMatch meeting = byId.get(common.iterator().next());
        assertThat(meeting.round()).isEqualTo(4);
    }

    private static List<TournamentMatchId> pathOf(List<TournamentMatch> matches,
                                                  Map<TournamentMatchId, TournamentMatch> byId, String participant) {
        TournamentMatch current = matches.stream().filter(m -> m.round() == 1
                && (name(m.red()).equals(participant) || name(m.blue()).equals(participant))).findFirst().orElseThrow();
        List<TournamentMatchId> path = new ArrayList<>();
        while (current != null) {
            path.add(current.id());
            current = current.winnerGoesTo() == null ? null : byId.get(current.winnerGoesTo().matchId());
        }
        return path;
    }

    @Test
    void theByesGoToTheBestSeeds() {
        List<TournamentMatch> matches = create(plain, 5);

        assertThat(round(matches, 1)).containsExactly("p1/-", "p4/p5", "p2/-", "p3/-");
    }

    @Test
    void aByeIsAlreadyCompletedAndTheEntrantGoesOn() {
        List<TournamentMatch> matches = create(plain, 5);

        TournamentMatch bye = matches.get(0);
        assertThat(bye.status()).isEqualTo(TournamentMatchStatus.COMPLETED);
        assertThat(bye.outcome().source()).isEqualTo(ResultSource.BYE);
        assertThat(bye.outcome().winnerSide()).isEqualTo(Side.RED);
        assertThat(bye.outcome().redScore()).isZero();
        assertThat(bye.outcome().blueScore()).isZero();
        assertThat(bye.outcome().completedAt()).isEqualTo(NOW);
        assertThat(bye.winner().id().value()).isEqualTo("p1");
        assertThat(bye.loser()).isNull();
        assertThat(round(matches, 2)).containsExactly("p1/-", "p2/p3");
        assertThat(matches.get(1).status()).isEqualTo(TournamentMatchStatus.READY);
    }

    @Test
    void threeEntrantsGiveTheFirstSeedAPlaceInTheFinal() {
        List<TournamentMatch> matches = create(plain, 3);

        assertThat(round(matches, 1)).containsExactly("p1/-", "p2/p3");
        assertThat(round(matches, 2)).containsExactly("p1/-");
        assertThat(matches).extracting(TournamentMatch::status).containsExactly(
                TournamentMatchStatus.COMPLETED, TournamentMatchStatus.READY, TournamentMatchStatus.PENDING);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 31, 32, 33, 64, 100, 127, 128})
    void everySizeMakesAWellFormedBracket(int count) {
        List<TournamentMatch> matches = create(plain, count);
        int size = sizeFor(count);
        int rounds = Integer.numberOfTrailingZeros(size);
        Map<TournamentMatchId, TournamentMatch> byId = byId(matches);

        assertThat(matches).hasSize(size - 1);
        assertThat(byId).hasSize(size - 1);
        for (int r = 1; r <= rounds; r++) {
            int round = r;
            assertThat(matches.stream().filter(m -> m.round() == round)).hasSize(size >> r);
        }
        assertThat(matches).isSortedAccordingTo((a, b) -> a.round() != b.round()
                ? Integer.compare(a.round(), b.round()) : Integer.compare(a.slot(), b.slot()));

        List<TournamentMatch> firstRound = matches.stream().filter(m -> m.round() == 1).toList();
        List<String> seen = firstRound.stream().flatMap(m -> java.util.stream.Stream.of(m.red(), m.blue()))
                .filter(e -> e != null).map(e -> e.id().value()).toList();
        assertThat(seen).containsExactlyInAnyOrderElementsOf(
                IntStream.rangeClosed(1, count).mapToObj(i -> "p" + i).toList());
        assertThat(firstRound.stream().filter(m -> m.outcome() != null)).hasSize(size - count);
        assertThat(firstRound.stream().filter(m -> m.outcome() != null))
                .allMatch(m -> m.outcome().source() == ResultSource.BYE);

        List<TournamentMatch> finals = matches.stream().filter(m -> m.winnerGoesTo() == null).toList();
        assertThat(finals).hasSize(1);
        assertThat(finals.get(0).round()).isEqualTo(rounds);
        assertThat(finals.get(0).slot()).isZero();

        Map<TournamentMatchId, Integer> fed = new java.util.HashMap<>();
        for (TournamentMatch match : matches) {
            if (match.winnerGoesTo() == null) {
                continue;
            }
            TournamentMatch next = byId.get(match.winnerGoesTo().matchId());
            assertThat(next).isNotNull();
            assertThat(next.round()).isEqualTo(match.round() + 1);
            assertThat(next.slot()).isEqualTo(match.slot() / 2);
            assertThat(match.winnerGoesTo().side()).isEqualTo(match.slot() % 2 == 0 ? Side.RED : Side.BLUE);
            fed.merge(next.id(), 1, Integer::sum);
        }
        assertThat(matches.stream().filter(m -> m.round() > 1)).allMatch(m -> fed.get(m.id()) == 2);
        assertThat(matches).allMatch(m -> m.loserGoesTo() == null);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 6, 7, 9, 12, 20})
    void aByeNeverMeetsAnotherByeAndTheSecondRoundIsFedByRealEntrants(int count) {
        List<TournamentMatch> matches = create(plain, count);

        for (TournamentMatch match : matches.stream().filter(m -> m.round() == 1).toList()) {
            assertThat(match.red()).isNotNull();
        }
        assertThat(matches.stream().filter(m -> m.round() == 2).flatMap(m -> java.util.stream.Stream.of(m.red(), m.blue())))
                .filteredOn(e -> e != null).allSatisfy(e -> assertThat(e.id().value()).startsWith("p"));
    }

    @Test
    void thirdPlaceTakesTheLosersOfTheSemifinals() {
        List<TournamentMatch> matches = create(withThirdPlace, 4);

        assertThat(matches).hasSize(4);
        TournamentMatch third = matches.get(3);
        assertThat(third.thirdPlace()).isTrue();
        assertThat(third.round()).isEqualTo(2);
        assertThat(third.slot()).isEqualTo(1);
        assertThat(third.status()).isEqualTo(TournamentMatchStatus.PENDING);
        assertThat(third.winnerGoesTo()).isNull();
        assertThat(third.loserGoesTo()).isNull();
        assertThat(matches.get(0).loserGoesTo().matchId()).isEqualTo(third.id());
        assertThat(matches.get(0).loserGoesTo().side()).isEqualTo(Side.RED);
        assertThat(matches.get(1).loserGoesTo().matchId()).isEqualTo(third.id());
        assertThat(matches.get(1).loserGoesTo().side()).isEqualTo(Side.BLUE);
        assertThat(matches.get(2).thirdPlace()).isFalse();
        assertThat(matches.get(2).loserGoesTo()).isNull();
    }

    @Test
    void onlyTheSemifinalsFeedTheThirdPlace() {
        List<TournamentMatch> matches = create(withThirdPlace, 16);

        assertThat(matches).hasSize(16);
        assertThat(matches.stream().filter(m -> m.loserGoesTo() != null)).hasSize(2)
                .allMatch(m -> m.round() == 3);
        assertThat(matches.stream().filter(TournamentMatch::thirdPlace)).hasSize(1)
                .allMatch(m -> m.round() == 4 && m.slot() == 1);
    }

    @Test
    void thereIsNoThirdPlaceWithLessThanFourEntrants() {
        assertThat(create(withThirdPlace, 3)).hasSize(3).noneMatch(TournamentMatch::thirdPlace);
        assertThat(create(withThirdPlace, 2)).hasSize(1).noneMatch(TournamentMatch::thirdPlace);
    }

    @Test
    void thereIsNoThirdPlaceUnlessAsked() {
        assertThat(create(plain, 8)).noneMatch(TournamentMatch::thirdPlace);
    }

    @Test
    void everyMatchGetsItsOwnIdFromTheSupplier() {
        List<TournamentMatchId> issued = new ArrayList<>();
        Supplier<TournamentMatchId> ids = () -> {
            TournamentMatchId id = new TournamentMatchId(UUID.randomUUID());
            issued.add(id);
            return id;
        };

        List<TournamentMatch> matches = withThirdPlace.createMatches(entrants(8), NOW, ids);

        assertThat(issued).hasSize(8);
        assertThat(matches).extracting(TournamentMatch::id).containsExactlyInAnyOrderElementsOf(issued);
    }

    @Test
    void theSameEntrantsGiveTheSameBracket() {
        List<TournamentMatch> first = plain.createMatches(entrants(11), NOW, sequentialIds());
        List<TournamentMatch> second = plain.createMatches(entrants(11), NOW, sequentialIds());

        assertThat(first).isEqualTo(second);
    }

    private static Supplier<TournamentMatchId> sequentialIds() {
        int[] next = {0};
        return () -> new TournamentMatchId(new UUID(0, next[0]++));
    }

    @Test
    void refusesLessThanTwoEntrants() {
        assertThatIllegalArgumentException().isThrownBy(() -> create(plain, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> plain.createMatches(List.of(), NOW, randomIds()));
    }

    @Test
    void refusesMoreThanTheMaximum() {
        assertThatIllegalArgumentException().isThrownBy(() -> create(plain, SingleElimination.MAX_ENTRANTS + 1));
        assertThat(create(plain, SingleElimination.MAX_ENTRANTS)).hasSize(127);
    }

    @Test
    void refusesTheSameParticipantTwice() {
        List<Entrant> twice = List.of(new Entrant(new ParticipantId("p1"), "Ada"),
                new Entrant(new ParticipantId("p2"), "Bruno"), new Entrant(new ParticipantId("p1"), "Ada again"));

        assertThatIllegalArgumentException().isThrownBy(() -> plain.createMatches(twice, NOW, randomIds()));
    }

    @Test
    void refusesMissingArguments() {
        assertThatNullPointerException().isThrownBy(() -> plain.createMatches(null, NOW, randomIds()));
        assertThatNullPointerException().isThrownBy(() -> plain.createMatches(entrants(4), null, randomIds()));
        assertThatNullPointerException().isThrownBy(() -> plain.createMatches(entrants(4), NOW, null));
    }
}
