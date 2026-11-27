package com.connectedgames.stats.adapters.out.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.MosquittoTestConfiguration;
import com.connectedgames.stats.TestcontainersConfiguration;
import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.application.port.out.StatsQueries;
import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.TournamentMatchId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.result.Outcome;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.Totals;
import com.connectedgames.stats.domain.stats.VenueSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.connectedgames.stats.MatchBuilder.match;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
class StatsQueriesPersistenceAdapterTest {

    private static final Instant T0 = Instant.parse("2026-11-10T20:00:00Z");
    private static final TenantId ACME = new TenantId("acme");
    private static final TenantId GLOBEX = new TenantId("globex");
    private static final Viewer ACME_MANAGER = new Viewer.OfTenants(Set.of(ACME));
    private static final Viewer GLOBEX_MANAGER = new Viewer.OfTenants(Set.of(GLOBEX));
    private static final Viewer BOTH_TENANTS = new Viewer.OfTenants(Set.of(ACME, GLOBEX));
    private static final GameTypeId FOOSBALL = Fixtures.GAME_TYPE;
    private static final GameTypeId CHESS = new GameTypeId(UUID.fromString("00000000-0000-4000-8000-0000000000b2"));

    @Autowired
    StatsQueries queries;
    @Autowired
    MatchResultRepository repository;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanTheTables() {
        jdbc.update("delete from match_result");
    }

    private MatchResult save(MatchResult result) {
        assertThat(repository.saveIfAbsent(result)).isTrue();
        return result;
    }

    private static Instant at(int minutes) {
        return T0.plus(Duration.ofMinutes(minutes));
    }

    private static Set<TenantId> acme() {
        return Set.of(ACME);
    }

    // ---- leaderboard

    @Test
    void theLeaderboardIsOrderedByWinsThenByPointDifference() {
        save(match().red("a", "A").blue("x", "X").score(5, 3).build());
        save(match().red("a", "A").blue("x", "X").score(5, 3).build());
        save(match().red("b", "B").blue("x", "X").score(5, 4).build());
        save(match().red("c", "C").blue("x", "X").score(5, 0).build());

        List<LeaderboardEntry> board = queries.leaderboard(acme(), StatsFilter.none(), 20);

        assertThat(board).extracting(LeaderboardEntry::participantId).containsExactly("a", "c", "b", "x");
        assertThat(board).extracting(LeaderboardEntry::rank).containsExactly(1, 2, 3, 4);
        assertThat(board.get(0).totals()).isEqualTo(new Totals(2, 2, 0, 0, 10, 6));
        assertThat(board.get(3).totals()).isEqualTo(new Totals(4, 0, 4, 0, 10, 20));
        assertThat(board.get(0).displayName()).isEqualTo("A");
    }

    @Test
    void participantsWithTheSameWinsAndDifferenceShareTheRank() {
        save(match().red("p1", "P1").blue("z1", "Z1").score(5, 3).build());
        save(match().red("p2", "P2").blue("z2", "Z2").score(5, 3).build());

        List<LeaderboardEntry> board = queries.leaderboard(acme(), StatsFilter.none(), 20);

        assertThat(board).extracting(LeaderboardEntry::participantId).containsExactly("p1", "p2", "z1", "z2");
        assertThat(board).extracting(LeaderboardEntry::rank).containsExactly(1, 1, 3, 3);
    }

    @Test
    void aDrawCountsAsADrawForBothSides() {
        save(match().red("a", "A").blue("b", "B").score(2, 2).build());

        List<LeaderboardEntry> board = queries.leaderboard(acme(), StatsFilter.none(), 20);

        assertThat(board).extracting(LeaderboardEntry::totals)
                .containsExactly(new Totals(1, 0, 0, 1, 2, 2), new Totals(1, 0, 0, 1, 2, 2));
        assertThat(board).extracting(LeaderboardEntry::rank).containsExactly(1, 1);
    }

    @Test
    void sidesWithoutALoginAreNotRanked() {
        save(match().red(null, null).blue(null, null).score(5, 3).build());
        save(match().red("a", "A").blue(null, null).score(5, 3).build());

        List<LeaderboardEntry> board = queries.leaderboard(acme(), StatsFilter.none(), 20);

        assertThat(board).extracting(LeaderboardEntry::participantId).containsExactly("a");
        assertThat(board.get(0).totals()).isEqualTo(new Totals(1, 1, 0, 0, 5, 3));
    }

    @Test
    void theNameIsTheLatestKnownOne() {
        save(match().red("a", "Old name").blue("x", null).score(5, 3).endedAt(at(1)).build());
        save(match().red("a", "New name").blue("x", null).score(5, 3).endedAt(at(2)).build());
        save(match().red("a", null).blue("x", null).score(5, 3).endedAt(at(3)).build());

        assertThat(queries.leaderboard(acme(), StatsFilter.none(), 20).get(0).displayName()).isEqualTo("New name");
        assertThat(queries.leaderboard(acme(), StatsFilter.none(), 20).get(1).displayName()).isNull();
    }

    @Test
    void theLimitCutsTheList() {
        save(match().red("a", "A").blue("b", "B").score(5, 3).build());
        save(match().red("c", "C").blue("d", "D").score(5, 3).build());

        assertThat(queries.leaderboard(acme(), StatsFilter.none(), 3)).hasSize(3);
        assertThat(queries.leaderboard(acme(), StatsFilter.none(), 1)).hasSize(1);
    }

    @Test
    void aLeaderboardWithoutMatchesIsEmpty() {
        assertThat(queries.leaderboard(acme(), StatsFilter.none(), 20)).isEmpty();
    }

    @Test
    void theLeaderboardCanBeFilteredByVenueGameTypeAndDates() {
        VenueId arena = new VenueId(UUID.randomUUID());
        VenueId bar = new VenueId(UUID.randomUUID());
        save(match().venue(arena).game(FOOSBALL).red("a", "A").blue("x", "X").score(5, 3).endedAt(at(10)).build());
        save(match().venue(arena).game(CHESS).red("b", "B").blue("x", "X").score(1, 0).endedAt(at(20)).build());
        save(match().venue(bar).game(FOOSBALL).red("c", "C").blue("x", "X").score(5, 1).endedAt(at(30)).build());

        assertThat(ids(queries.leaderboard(acme(), new StatsFilter(arena, null, null, null), 20)))
                .containsExactlyInAnyOrder("a", "b", "x");
        assertThat(ids(queries.leaderboard(acme(), new StatsFilter(null, CHESS, null, null), 20)))
                .containsExactlyInAnyOrder("b", "x");
        assertThat(ids(queries.leaderboard(acme(), new StatsFilter(arena, FOOSBALL, null, null), 20)))
                .containsExactlyInAnyOrder("a", "x");
        assertThat(ids(queries.leaderboard(acme(), new StatsFilter(null, null, at(20), null), 20)))
                .containsExactlyInAnyOrder("b", "c", "x");
        assertThat(ids(queries.leaderboard(acme(), new StatsFilter(null, null, null, at(20)), 20)))
                .containsExactlyInAnyOrder("a", "x");
        assertThat(ids(queries.leaderboard(acme(), new StatsFilter(null, null, at(20), at(30)), 20)))
                .containsExactlyInAnyOrder("b", "x");
    }

    private static List<String> ids(List<LeaderboardEntry> board) {
        return board.stream().map(LeaderboardEntry::participantId).toList();
    }

    @Test
    void theLeaderboardOfAnOrganizationNeverCountsTheMatchesOfAnother() {
        save(match().tenant("acme").red("shared", "Shared").blue("x", "X").score(5, 3).build());
        save(match().tenant("globex").red("shared", "Shared").blue("y", "Y").score(5, 3).build());
        save(match().tenant("globex").red("shared", "Shared").blue("y", "Y").score(5, 3).build());

        List<LeaderboardEntry> acmeBoard = queries.leaderboard(Set.of(ACME), StatsFilter.none(), 20);
        List<LeaderboardEntry> globexBoard = queries.leaderboard(Set.of(GLOBEX), StatsFilter.none(), 20);
        List<LeaderboardEntry> both = queries.leaderboard(Set.of(ACME, GLOBEX), StatsFilter.none(), 20);

        assertThat(ids(acmeBoard)).containsExactly("shared", "x");
        assertThat(acmeBoard.get(0).totals().matches()).isEqualTo(1);
        assertThat(ids(globexBoard)).containsExactly("shared", "y");
        assertThat(globexBoard.get(0).totals().matches()).isEqualTo(2);
        assertThat(both.get(0).totals().matches()).isEqualTo(3);
        assertThat(queries.leaderboard(Set.of(new TenantId("initech")), StatsFilter.none(), 20)).isEmpty();
    }

    // ---- participantKnown

    @Test
    void aParticipantIsKnownOnlyInTheTenantsWhereItPlayed() {
        save(match().tenant("acme").red("a", "A").blue("x", "X").build());

        assertThat(queries.participantKnown(ACME_MANAGER, "a")).isTrue();
        assertThat(queries.participantKnown(GLOBEX_MANAGER, "a")).isFalse();
        assertThat(queries.participantKnown(ACME_MANAGER, "nobody")).isFalse();
    }

    @Test
    void aPlayerKnowsItselfInAnyOrganizationAndNobodyElse() {
        save(match().tenant("acme").red("a", "A").blue("x", "X").build());
        save(match().tenant("globex").red("a", "A").blue("y", "Y").build());

        assertThat(queries.participantKnown(new Viewer.OfPlayer("a"), "a")).isTrue();
        assertThat(queries.participantKnown(new Viewer.OfPlayer("x"), "a")).isFalse();
        assertThat(queries.participantKnown(new Viewer.OfPlayer("a"), "x")).isFalse();
        assertThat(queries.participantKnown(new Viewer.OfPlayer("z"), "z")).isFalse();
    }

    // ---- participantSummary

    @Test
    void theSummaryAddsUpTheMatchesAndSplitsThemByGameType() {
        save(match().game(FOOSBALL).red("a", "A").blue("x", "X").score(5, 3).endedAt(at(1)).build());
        save(match().game(FOOSBALL).red("a", "A").blue("x", "X").score(2, 5).endedAt(at(2)).build());
        save(match().game(FOOSBALL).red("x", "X").blue("a", "A").score(2, 2).endedAt(at(3)).build());
        save(match().game(CHESS).red("a", "A").blue("x", "X").score(1, 0).endedAt(at(4)).build());

        ParticipantSummary summary = queries.participantSummary(ACME_MANAGER, "a", StatsFilter.none());

        assertThat(summary.participantId()).isEqualTo("a");
        assertThat(summary.displayName()).isEqualTo("A");
        assertThat(summary.lastMatchAt()).isEqualTo(at(4));
        assertThat(summary.totals()).isEqualTo(new Totals(4, 2, 1, 1, 10, 10));
        assertThat(summary.byGameType()).hasSize(2);
        assertThat(summary.byGameType().get(0).gameTypeId()).isEqualTo(FOOSBALL);
        assertThat(summary.byGameType().get(0).totals()).isEqualTo(new Totals(3, 1, 1, 1, 9, 10));
        assertThat(summary.byGameType().get(1).gameTypeId()).isEqualTo(CHESS);
        assertThat(summary.byGameType().get(1).totals()).isEqualTo(new Totals(1, 1, 0, 0, 1, 0));
    }

    @Test
    void aFilterThatExcludesEveryMatchGivesZerosButKeepsTheName() {
        save(match().red("a", "A").blue("x", "X").endedAt(at(10)).build());

        ParticipantSummary summary = queries.participantSummary(ACME_MANAGER, "a",
                new StatsFilter(null, null, at(20), null));

        assertThat(summary.totals()).isEqualTo(Totals.none());
        assertThat(summary.byGameType()).isEmpty();
        assertThat(summary.lastMatchAt()).isNull();
        assertThat(summary.displayName()).isEqualTo("A");
    }

    @Test
    void aSummaryOfAnOrganizationNeverCountsTheMatchesOfAnother() {
        save(match().tenant("acme").red("shared", "Shared").blue("x", "X").score(5, 3).build());
        save(match().tenant("globex").red("shared", "Shared").blue("y", "Y").score(1, 5).build());

        assertThat(queries.participantSummary(ACME_MANAGER, "shared", StatsFilter.none()).totals())
                .isEqualTo(new Totals(1, 1, 0, 0, 5, 3));
        assertThat(queries.participantSummary(GLOBEX_MANAGER, "shared", StatsFilter.none()).totals())
                .isEqualTo(new Totals(1, 0, 1, 0, 1, 5));
        assertThat(queries.participantSummary(new Viewer.OfPlayer("shared"), "shared", StatsFilter.none()).totals())
                .isEqualTo(new Totals(2, 1, 1, 0, 6, 8));
    }

    @Test
    void aPlayerCannotGetTheSummaryOfAnotherParticipantEvenIfItAsksForIt() {
        save(match().red("a", "A").blue("x", "X").score(5, 3).build());

        ParticipantSummary summary = queries.participantSummary(new Viewer.OfPlayer("x"), "a", StatsFilter.none());

        assertThat(summary.totals()).isEqualTo(Totals.none());
    }

    // ---- participantMatches

    @Test
    void theMatchesAreListedNewestFirstAndPaged() {
        MatchResult first = save(match().red("a", "A").blue("x", "X").endedAt(at(1)).build());
        MatchResult second = save(match().red("a", "A").blue("x", "X").endedAt(at(2)).build());
        MatchResult third = save(match().red("a", "A").blue("x", "X").endedAt(at(3)).build());

        List<ParticipantMatch> all = queries.participantMatches(ACME_MANAGER, "a", StatsFilter.none(), new Page(20, 0));
        List<ParticipantMatch> page = queries.participantMatches(ACME_MANAGER, "a", StatsFilter.none(), new Page(1, 1));

        assertThat(all).extracting(ParticipantMatch::matchId).containsExactly(third.id(), second.id(), first.id());
        assertThat(page).extracting(ParticipantMatch::matchId).containsExactly(second.id());
        assertThat(queries.participantMatches(ACME_MANAGER, "a", StatsFilter.none(), new Page(5, 3))).isEmpty();
    }

    @Test
    void aMatchIsSeenFromTheSideOfTheParticipant() {
        TournamentMatchId bracket = Fixtures.TOURNAMENT_MATCH;
        MatchResult won = save(match().tournament(bracket).red("a", "A").blue("x", "Xavier").score(5, 3)
                .duration(Duration.ofSeconds(425)).endedAt(at(1)).build());
        MatchResult lost = save(match().red("x", "Xavier").blue("a", "A").score(5, 2).endedAt(at(2)).build());
        MatchResult drawn = save(match().red("a", "A").blue("x", null).score(2, 2).endedAt(at(3)).build());

        List<ParticipantMatch> matches = queries.participantMatches(ACME_MANAGER, "a", StatsFilter.none(), Page.first());

        ParticipantMatch d = matches.get(0);
        assertThat(d.matchId()).isEqualTo(drawn.id());
        assertThat(d.result()).isEqualTo(Outcome.DRAW);
        assertThat(d.opponentParticipantId()).isEqualTo("x");
        assertThat(d.opponentDisplayName()).isNull();
        assertThat(d.tournamentMatchId()).isNull();

        ParticipantMatch l = matches.get(1);
        assertThat(l.matchId()).isEqualTo(lost.id());
        assertThat(l.side()).isEqualTo(Side.BLUE);
        assertThat(l.result()).isEqualTo(Outcome.LOSS);
        assertThat(l.pointsFor()).isEqualTo(2);
        assertThat(l.pointsAgainst()).isEqualTo(5);
        assertThat(l.opponentParticipantId()).isEqualTo("x");
        assertThat(l.opponentDisplayName()).isEqualTo("Xavier");

        ParticipantMatch w = matches.get(2);
        assertThat(w.matchId()).isEqualTo(won.id());
        assertThat(w.side()).isEqualTo(Side.RED);
        assertThat(w.result()).isEqualTo(Outcome.WIN);
        assertThat(w.pointsFor()).isEqualTo(5);
        assertThat(w.pointsAgainst()).isEqualTo(3);
        assertThat(w.durationSeconds()).isEqualTo(425);
        assertThat(w.endedAt()).isEqualTo(at(1));
        assertThat(w.tournamentMatchId()).isEqualTo(bracket);
        assertThat(w.venueId()).isEqualTo(won.venueId());
        assertThat(w.tableId()).isEqualTo(won.tableId());
        assertThat(w.gameTypeId()).isEqualTo(won.gameTypeId());
    }

    @Test
    void theMatchesOfAParticipantCanBeFiltered() {
        VenueId arena = new VenueId(UUID.randomUUID());
        MatchResult inArena = save(match().venue(arena).game(FOOSBALL).red("a", "A").blue("x", "X").endedAt(at(10)).build());
        MatchResult chess = save(match().game(CHESS).red("a", "A").blue("x", "X").endedAt(at(20)).build());

        assertThat(queries.participantMatches(ACME_MANAGER, "a", new StatsFilter(arena, null, null, null), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(inArena.id());
        assertThat(queries.participantMatches(ACME_MANAGER, "a", new StatsFilter(null, CHESS, null, null), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(chess.id());
        assertThat(queries.participantMatches(ACME_MANAGER, "a", new StatsFilter(null, null, at(20), at(21)), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(chess.id());
        assertThat(queries.participantMatches(ACME_MANAGER, "a", new StatsFilter(null, null, null, at(20)), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(inArena.id());
    }

    @Test
    void theMatchesOfAnotherOrganizationAreNeverListed() {
        MatchResult mine = save(match().tenant("acme").red("shared", "S").blue("x", "X").endedAt(at(1)).build());
        MatchResult theirs = save(match().tenant("globex").red("shared", "S").blue("y", "Y").endedAt(at(2)).build());

        assertThat(queries.participantMatches(ACME_MANAGER, "shared", StatsFilter.none(), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(mine.id());
        assertThat(queries.participantMatches(GLOBEX_MANAGER, "shared", StatsFilter.none(), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(theirs.id());
        assertThat(queries.participantMatches(new Viewer.OfPlayer("shared"), "shared", StatsFilter.none(), Page.first()))
                .extracting(ParticipantMatch::matchId).containsExactly(theirs.id(), mine.id());
    }

    // ---- matchResult

    @Test
    void aMatchComesBackExactlyAsItWasSaved() {
        MatchResult saved = save(match().tournament(Fixtures.TOURNAMENT_MATCH).red("team-1", "Lightning")
                .blue("team-2", "Thunder").score(5, 3).points("RRBRBRBR").build());

        assertThat(queries.matchResult(ACME_MANAGER, saved.id())).contains(saved);
    }

    @Test
    void aMatchPlayedWithoutLoginComesBackWithoutNames() {
        MatchResult saved = save(Fixtures.draw(new MatchId(UUID.randomUUID())));

        assertThat(queries.matchResult(ACME_MANAGER, saved.id())).contains(saved);
    }

    @Test
    void aMatchOfAnotherOrganizationIsNotFound() {
        MatchResult theirs = save(match().tenant("globex").red("a", "A").blue("x", "X").build());

        assertThat(queries.matchResult(ACME_MANAGER, theirs.id())).isEmpty();
        assertThat(queries.matchResult(GLOBEX_MANAGER, theirs.id())).isPresent();
        assertThat(queries.matchResult(BOTH_TENANTS, theirs.id())).isPresent();
        assertThat(queries.matchResult(ACME_MANAGER, new MatchId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void aPlayerSeesTheMatchesItTookPartInAndNoOthers() {
        MatchResult saved = save(match().tenant("globex").red("a", "A").blue("x", "X").build());

        assertThat(queries.matchResult(new Viewer.OfPlayer("a"), saved.id())).contains(saved);
        assertThat(queries.matchResult(new Viewer.OfPlayer("x"), saved.id())).contains(saved);
        assertThat(queries.matchResult(new Viewer.OfPlayer("z"), saved.id())).isEmpty();
    }

    // ---- venue

    @Test
    void aVenueIsKnownOnlyInItsOwnTenant() {
        VenueId arena = new VenueId(UUID.randomUUID());
        save(match().tenant("acme").venue(arena).red("a", "A").blue("x", "X").build());

        assertThat(queries.venueKnown(Set.of(ACME), arena)).isTrue();
        assertThat(queries.venueKnown(Set.of(GLOBEX), arena)).isFalse();
        assertThat(queries.venueKnown(Set.of(ACME), new VenueId(UUID.randomUUID()))).isFalse();
    }

    @Test
    void theSummaryOfAVenueAddsUpPointsAndAveragesTheDuration() {
        VenueId arena = new VenueId(UUID.randomUUID());
        save(match().venue(arena).game(FOOSBALL).score(5, 3).duration(Duration.ofSeconds(100)).endedAt(at(1)).build());
        save(match().venue(arena).game(FOOSBALL).score(5, 4).duration(Duration.ofSeconds(201)).endedAt(at(2)).build());
        save(match().venue(arena).game(CHESS).score(1, 0).duration(Duration.ofSeconds(900)).endedAt(at(3)).build());
        save(match().venue(new VenueId(UUID.randomUUID())).score(5, 5).endedAt(at(4)).build());

        VenueSummary summary = queries.venueSummary(acme(), arena, StatsFilter.none());

        assertThat(summary.venueId()).isEqualTo(arena);
        assertThat(summary.matches()).isEqualTo(3);
        assertThat(summary.totalPoints()).isEqualTo(18);
        assertThat(summary.averageDurationSeconds()).isEqualTo(400);
        assertThat(summary.lastMatchAt()).isEqualTo(at(3));
        assertThat(summary.byGameType()).hasSize(2);
        assertThat(summary.byGameType().get(0).gameTypeId()).isEqualTo(FOOSBALL);
        assertThat(summary.byGameType().get(0).matches()).isEqualTo(2);
        assertThat(summary.byGameType().get(0).totalPoints()).isEqualTo(17);
        assertThat(summary.byGameType().get(0).averageDurationSeconds()).isEqualTo(151);
        assertThat(summary.byGameType().get(1).gameTypeId()).isEqualTo(CHESS);
        assertThat(summary.byGameType().get(1).averageDurationSeconds()).isEqualTo(900);
    }

    @Test
    void theSummaryOfAVenueCanBeFilteredByGameTypeAndDates() {
        VenueId arena = new VenueId(UUID.randomUUID());
        save(match().venue(arena).game(FOOSBALL).score(5, 3).endedAt(at(10)).build());
        save(match().venue(arena).game(CHESS).score(1, 0).endedAt(at(20)).build());

        assertThat(queries.venueSummary(acme(), arena, new StatsFilter(null, CHESS, null, null)).matches()).isEqualTo(1);
        assertThat(queries.venueSummary(acme(), arena, new StatsFilter(null, null, at(10), at(20))).matches()).isEqualTo(1);
        VenueSummary none = queries.venueSummary(acme(), arena, new StatsFilter(null, null, at(50), null));
        assertThat(none.matches()).isZero();
        assertThat(none.averageDurationSeconds()).isZero();
        assertThat(none.lastMatchAt()).isNull();
        assertThat(none.byGameType()).isEmpty();
    }

    @Test
    void aVenueIdInTheFilterDoesNotChangeTheVenueAsked() {
        VenueId arena = new VenueId(UUID.randomUUID());
        save(match().venue(arena).build());
        save(match().venue(new VenueId(UUID.randomUUID())).build());

        VenueSummary summary = queries.venueSummary(acme(), arena,
                new StatsFilter(new VenueId(UUID.randomUUID()), null, null, null));

        assertThat(summary.matches()).isEqualTo(1);
    }

    @Test
    void theSummaryOfAVenueNeverCountsTheMatchesOfAnotherOrganization() {
        VenueId arena = new VenueId(UUID.randomUUID());
        save(match().tenant("acme").venue(arena).build());
        save(match().tenant("globex").venue(arena).build());

        assertThat(queries.venueSummary(Set.of(ACME), arena, StatsFilter.none()).matches()).isEqualTo(1);
        assertThat(queries.venueSummary(Set.of(GLOBEX), arena, StatsFilter.none()).matches()).isEqualTo(1);
        assertThat(queries.venueSummary(Set.of(ACME, GLOBEX), arena, StatsFilter.none()).matches()).isEqualTo(2);
        assertThat(queries.venueSummary(Set.of(new TenantId("initech")), arena, StatsFilter.none()).matches()).isZero();
    }

    // ---- isolation of the whole read side

    @Test
    void anOrganizationWithoutMatchesSeesNothingOfTheOthers() {
        VenueId theirVenue = new VenueId(UUID.randomUUID());
        MatchResult theirs = save(match().tenant("globex").venue(theirVenue).red("g1", "G1").blue("g2", "G2")
                .points("RRB").build());
        Viewer initech = new Viewer.OfTenants(Set.of(new TenantId("initech")));

        assertThat(queries.leaderboard(Set.of(new TenantId("initech")), StatsFilter.none(), 100)).isEmpty();
        assertThat(queries.participantKnown(initech, "g1")).isFalse();
        assertThat(queries.participantSummary(initech, "g1", StatsFilter.none()).totals()).isEqualTo(Totals.none());
        assertThat(queries.participantMatches(initech, "g1", StatsFilter.none(), Page.first())).isEmpty();
        assertThat(queries.matchResult(initech, theirs.id())).isEmpty();
        assertThat(queries.venueKnown(Set.of(new TenantId("initech")), theirVenue)).isFalse();
        assertThat(queries.venueSummary(Set.of(new TenantId("initech")), theirVenue, StatsFilter.none()).matches())
                .isZero();
    }
}
