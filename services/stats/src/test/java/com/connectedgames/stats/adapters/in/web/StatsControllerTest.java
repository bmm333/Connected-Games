package com.connectedgames.stats.adapters.in.web;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.TableId;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.TournamentMatchId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchDetail;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.result.Outcome;
import com.connectedgames.stats.domain.stats.GameTypeTotals;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.Totals;
import com.connectedgames.stats.domain.stats.VenueGameTypeTotals;
import com.connectedgames.stats.domain.stats.VenueSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.connectedgames.stats.MatchBuilder.match;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@Import({SecurityConfiguration.class, StubUseCases.class})
class StatsControllerTest {

    private static final OpenApiContract CONTRACT = OpenApiContract.load();
    private static final Instant T = Instant.parse("2026-11-16T20:00:00Z");
    private static final Totals TOTALS = new Totals(3, 2, 0, 1, 12, 7);
    private static final GameTypeId FOOSBALL = Fixtures.GAME_TYPE;
    private static final String ID = UUID.randomUUID().toString();

    @Autowired
    MockMvc mvc;
    @Autowired
    StubUseCases use;

    @BeforeEach
    void reset() {
        use.reset();
    }

    private MvcResult call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn();
    }

    private void assertContract(String path, MvcResult result) throws Exception {
        List<String> errors = CONTRACT.validateResponse(path, result.getResponse().getStatus(),
                result.getResponse().getContentAsString());
        assertThat(errors).as("%s answered %s: %s", path, result.getResponse().getStatus(),
                result.getResponse().getContentAsString()).isEmpty();
    }

    // ---- authentication

    @Test
    void everyRouteNeedsAToken() throws Exception {
        for (String path : List.of("/leaderboard", "/participants/team-1/summary", "/participants/team-1/matches",
                "/matches/" + ID, "/venues/" + ID + "/summary")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Test
    void aTokenThatIsNotSignedIsRefused() throws Exception {
        mvc.perform(get("/leaderboard").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    // ---- who is the viewer

    @Test
    void aManagerReadsTheOrganizationsOfItsRoles() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme", "globex"))).andExpect(status().isOk());

        assertThat(use.lastViewer).isEqualTo(new Viewer.OfTenants(Set.of(new TenantId("acme"), new TenantId("globex"))));
    }

    @Test
    void aManagerWhoIsAlsoAPlayerIsReadAsAManager() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.managerAndPlayer("acme", "player-1"))).andExpect(status().isOk());

        assertThat(use.lastViewer).isEqualTo(new Viewer.OfTenants(Set.of(new TenantId("acme"))));
    }

    @Test
    void aDisplayReadsItsOwnOrganization() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.display("acme"))).andExpect(status().isOk());

        assertThat(use.lastViewer).isEqualTo(new Viewer.OfTenants(Set.of(new TenantId("acme"))));
    }

    @Test
    void aPlayerIsReadAsItsPlayerId() throws Exception {
        use.summary = Optional.of(new ParticipantSummary("player-1", null, null, Totals.none(), List.of()));

        mvc.perform(get("/participants/player-1/summary").with(JwtTokens.player("player-1"))).andExpect(status().isOk());

        assertThat(use.lastViewer).isEqualTo(new Viewer.OfPlayer("player-1"));
    }

    @Test
    void aGuestIsReadAsItsSubjectOrItsPlayerClaim() throws Exception {
        use.summary = Optional.of(new ParticipantSummary("p", null, null, Totals.none(), List.of()));

        mvc.perform(get("/participants/p/summary").with(JwtTokens.guest("guest-player"))).andExpect(status().isOk());
        assertThat(use.lastViewer).isEqualTo(new Viewer.OfPlayer("guest-player"));

        mvc.perform(get("/participants/p/summary").with(JwtTokens.guestWithPlayerClaim("subject", "claimed")))
                .andExpect(status().isOk());
        assertThat(use.lastViewer).isEqualTo(new Viewer.OfPlayer("claimed"));
    }

    @Test
    void aTokenThatSaysNothingUsefulIsForbiddenWithoutAskingTheUseCases() throws Exception {
        List<org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor> unusable = List.of(
                JwtTokens.playerWithoutPlayerId(), JwtTokens.admin("player-1"), JwtTokens.displayWithoutOrganization(),
                JwtTokens.withoutKind(), JwtTokens.ofKind("ROBOT"));
        for (var token : unusable) {
            mvc.perform(get("/leaderboard").with(token)).andExpect(status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(403));
        }
        assertThat(use.lastViewer).isNull();
    }

    @Test
    void whatTheUseCaseRefusesIsForbidden() throws Exception {
        use.forbidden = true;

        MvcResult result = call(get("/leaderboard").with(JwtTokens.player("player-1")));

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertContract("/leaderboard", result);
    }

    // ---- parameters

    @Test
    void theDefaultsAreTwentyAndZero() throws Exception {
        use.matches = Optional.of(List.of());

        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme"))).andExpect(status().isOk());
        mvc.perform(get("/participants/p/matches").with(JwtTokens.manager("acme"))).andExpect(status().isOk());

        assertThat(use.lastLimit).isEqualTo(20);
        assertThat(use.lastPage.limit()).isEqualTo(20);
        assertThat(use.lastPage.offset()).isZero();
        assertThat(use.lastFilter.venueId()).isNull();
    }

    @Test
    void theFiltersReachTheUseCase() throws Exception {
        UUID venue = UUID.randomUUID();
        UUID game = UUID.randomUUID();
        use.matches = Optional.of(List.of());

        mvc.perform(get("/participants/team-1/matches").with(JwtTokens.manager("acme"))
                .param("venueId", venue.toString()).param("gameTypeId", game.toString())
                .param("from", "2026-11-01T00:00:00Z").param("to", "2026-12-01T00:00:00+01:00")
                .param("limit", "5").param("offset", "10")).andExpect(status().isOk());

        assertThat(use.lastParticipantId).isEqualTo("team-1");
        assertThat(use.lastFilter.venueId()).isEqualTo(new VenueId(venue));
        assertThat(use.lastFilter.gameTypeId()).isEqualTo(new GameTypeId(game));
        assertThat(use.lastFilter.from()).isEqualTo(Instant.parse("2026-11-01T00:00:00Z"));
        assertThat(use.lastFilter.to()).isEqualTo(Instant.parse("2026-11-30T23:00:00Z"));
        assertThat(use.lastPage.limit()).isEqualTo(5);
        assertThat(use.lastPage.offset()).isEqualTo(10);
    }

    @Test
    void theFiltersOfTheLeaderboardAndOfTheSummariesReachTheUseCaseToo() throws Exception {
        UUID venue = UUID.randomUUID();
        UUID game = UUID.randomUUID();
        use.summary = Optional.of(new ParticipantSummary("team-1", null, null, Totals.none(), List.of()));
        use.venueSummary = Optional.of(new VenueSummary(new VenueId(venue), 0, 0, 0, null, List.of()));

        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme")).param("venueId", venue.toString())
                .param("gameTypeId", game.toString()).param("from", "2026-11-01T00:00:00Z")
                .param("to", "2026-12-01T00:00:00Z")).andExpect(status().isOk());
        assertThat(use.lastFilter).isEqualTo(new com.connectedgames.stats.domain.stats.StatsFilter(new VenueId(venue),
                new GameTypeId(game), Instant.parse("2026-11-01T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z")));

        mvc.perform(get("/participants/team-1/summary").with(JwtTokens.manager("acme"))
                .param("venueId", venue.toString()).param("gameTypeId", game.toString())
                .param("from", "2026-11-01T00:00:00Z").param("to", "2026-12-01T00:00:00Z")).andExpect(status().isOk());
        assertThat(use.lastFilter).isEqualTo(new com.connectedgames.stats.domain.stats.StatsFilter(new VenueId(venue),
                new GameTypeId(game), Instant.parse("2026-11-01T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z")));

        mvc.perform(get("/venues/{id}/summary", venue).with(JwtTokens.manager("acme"))
                .param("gameTypeId", game.toString()).param("from", "2026-11-01T00:00:00Z")
                .param("to", "2026-12-01T00:00:00Z")).andExpect(status().isOk());
        assertThat(use.lastFilter).isEqualTo(new com.connectedgames.stats.domain.stats.StatsFilter(null,
                new GameTypeId(game), Instant.parse("2026-11-01T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z")));
    }

    @Test
    void theLeaderboardLimitReachesTheUseCase() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme")).param("limit", "100")).andExpect(status().isOk());

        assertThat(use.lastLimit).isEqualTo(100);
    }

    @Test
    void aParameterOutOfRangeOrMalformedIsABadRequest() throws Exception {
        use.summary = Optional.of(new ParticipantSummary("p", null, null, Totals.none(), List.of()));
        List<MockHttpServletRequestBuilder> bad = List.of(
                get("/leaderboard").param("limit", "0"), get("/leaderboard").param("limit", "101"),
                get("/leaderboard").param("limit", "many"),
                get("/leaderboard").param("venueId", "not-a-uuid"),
                get("/leaderboard").param("gameTypeId", "123"),
                get("/leaderboard").param("from", "yesterday"),
                get("/leaderboard").param("from", "2026-12-01T00:00:00Z").param("to", "2026-11-01T00:00:00Z"),
                get("/participants/p/matches").param("offset", "-1"),
                get("/participants/p/matches").param("limit", "0"),
                get("/participants/p/summary").param("from", "2026-12-01T00:00:00Z").param("to", "2026-11-01T00:00:00Z"),
                get("/matches/not-a-uuid"), get("/venues/42/summary"));
        for (MockHttpServletRequestBuilder request : bad) {
            MvcResult result = call(request.with(JwtTokens.manager("acme")));
            assertThat(result.getResponse().getStatus()).as(result.getRequest().getRequestURI() + " "
                    + result.getRequest().getQueryString()).isEqualTo(400);
            assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
        }
    }

    // ---- not found

    @Test
    void whatTheViewerCannotSeeIsNotFound() throws Exception {
        for (String path : List.of("/participants/team-1/summary", "/participants/team-1/matches", "/matches/" + ID,
                "/venues/" + ID + "/summary")) {
            MvcResult result = call(get(path).with(JwtTokens.manager("acme")));
            assertThat(result.getResponse().getStatus()).as(path).isEqualTo(404);
            assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
        }
    }

    // ---- the bodies are the ones of the contract

    @Test
    void theLeaderboardIsAsTheContractSays() throws Exception {
        use.leaderboard = List.of(new LeaderboardEntry(1, "team-1", "Lightning", TOTALS),
                new LeaderboardEntry(2, "team-2", null, Totals.none()));

        MvcResult result = call(get("/leaderboard").with(JwtTokens.manager("acme")));

        assertContract("/leaderboard", result);
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].participantId").value("team-1"))
                .andExpect(jsonPath("$[0].displayName").value("Lightning"))
                .andExpect(jsonPath("$[0].matches").value(3))
                .andExpect(jsonPath("$[0].wins").value(2))
                .andExpect(jsonPath("$[0].draws").value(1))
                .andExpect(jsonPath("$[0].pointsFor").value(12))
                .andExpect(jsonPath("$[0].pointsAgainst").value(7))
                .andExpect(jsonPath("$[1].displayName").doesNotExist());
    }

    @Test
    void anEmptyLeaderboardIsAnEmptyList() throws Exception {
        MvcResult result = call(get("/leaderboard").with(JwtTokens.manager("acme")));

        assertContract("/leaderboard", result);
        assertThat(result.getResponse().getContentAsString()).isEqualTo("[]");
    }

    @Test
    void theSummaryOfAParticipantIsAsTheContractSays() throws Exception {
        use.summary = Optional.of(new ParticipantSummary("team-1", "Lightning", T, TOTALS,
                List.of(new GameTypeTotals(FOOSBALL, TOTALS))));

        MvcResult result = call(get("/participants/team-1/summary").with(JwtTokens.manager("acme")));

        assertContract("/participants/{participantId}/summary", result);
        mvc.perform(get("/participants/team-1/summary").with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$.lastMatchAt").value("2026-11-16T20:00:00Z"))
                .andExpect(jsonPath("$.matches").value(3))
                .andExpect(jsonPath("$.byGameType[0].gameTypeId").value(FOOSBALL.value().toString()))
                .andExpect(jsonPath("$.byGameType[0].wins").value(2));
    }

    @Test
    void aSummaryWithoutMatchesIsStillAsTheContractSays() throws Exception {
        use.summary = Optional.of(new ParticipantSummary("team-1", null, null, Totals.none(), List.of()));

        MvcResult result = call(get("/participants/team-1/summary").with(JwtTokens.manager("acme")));

        assertContract("/participants/{participantId}/summary", result);
        mvc.perform(get("/participants/team-1/summary").with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$.lastMatchAt").doesNotExist())
                .andExpect(jsonPath("$.byGameType").isEmpty());
    }

    @Test
    void theMatchesOfAParticipantAreAsTheContractSays() throws Exception {
        use.matches = Optional.of(List.of(
                new ParticipantMatch(new MatchId(UUID.randomUUID()), new VenueId(UUID.randomUUID()),
                        new TableId(UUID.randomUUID()), FOOSBALL, Fixtures.TOURNAMENT_MATCH, T, 425, Side.BLUE,
                        Outcome.LOSS, 3, 5, "team-2", "Thunder"),
                new ParticipantMatch(new MatchId(UUID.randomUUID()), new VenueId(UUID.randomUUID()),
                        new TableId(UUID.randomUUID()), FOOSBALL, null, T, 60, Side.RED, Outcome.DRAW, 2, 2,
                        null, null)));

        MvcResult result = call(get("/participants/team-1/matches").with(JwtTokens.manager("acme")));

        assertContract("/participants/{participantId}/matches", result);
        mvc.perform(get("/participants/team-1/matches").with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$[0].side").value("BLUE"))
                .andExpect(jsonPath("$[0].result").value("LOSS"))
                .andExpect(jsonPath("$[0].durationSeconds").value(425))
                .andExpect(jsonPath("$[0].tournamentMatchId").value(Fixtures.TOURNAMENT_MATCH.value().toString()))
                .andExpect(jsonPath("$[0].opponentParticipantId").value("team-2"))
                .andExpect(jsonPath("$[1].tournamentMatchId").doesNotExist())
                .andExpect(jsonPath("$[1].opponentParticipantId").doesNotExist());
    }

    @Test
    void aMatchWithItsPointsIsAsTheContractSays() throws Exception {
        MatchResult played = match().tournament(new TournamentMatchId(UUID.randomUUID())).red("team-1", "Lightning")
                .blue("team-2", "Thunder").score(5, 3).points("RRBRBRBR").build();
        use.matchResult = Optional.of(MatchDetail.of(played));

        MvcResult result = call(get("/matches/{id}", played.id().value()).with(JwtTokens.manager("acme")));

        assertContract("/matches/{matchId}", result);
        mvc.perform(get("/matches/{id}", played.id().value()).with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$.matchId").value(played.id().value().toString()))
                .andExpect(jsonPath("$.mode").value("TEAM"))
                .andExpect(jsonPath("$.reason").value("SCORE_REACHED"))
                .andExpect(jsonPath("$.winnerSide").value("RED"))
                .andExpect(jsonPath("$.durationSeconds").value(600))
                .andExpect(jsonPath("$.sides[0].side").value("RED"))
                .andExpect(jsonPath("$.sides[1].side").value("BLUE"))
                .andExpect(jsonPath("$.points.length()").value(8))
                .andExpect(jsonPath("$.points[0].redScore").value(1))
                .andExpect(jsonPath("$.points[0].blueScore").value(0))
                .andExpect(jsonPath("$.points[7].redScore").value(5))
                .andExpect(jsonPath("$.points[7].blueScore").value(3))
                .andExpect(jsonPath("$.leadChanges").value(0))
                .andExpect(jsonPath("$.longestStreak.side").value("RED"))
                .andExpect(jsonPath("$.longestStreak.length").value(2));
    }

    @Test
    void aMatchWithoutPointsOrNamesIsStillAsTheContractSays() throws Exception {
        MatchResult drawn = Fixtures.draw(new MatchId(UUID.randomUUID()));
        use.matchResult = Optional.of(MatchDetail.of(drawn));

        MvcResult result = call(get("/matches/{id}", drawn.id().value()).with(JwtTokens.manager("acme")));

        assertContract("/matches/{matchId}", result);
        mvc.perform(get("/matches/{id}", drawn.id().value()).with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$.winnerSide").doesNotExist())
                .andExpect(jsonPath("$.tournamentMatchId").doesNotExist())
                .andExpect(jsonPath("$.longestStreak").doesNotExist())
                .andExpect(jsonPath("$.points").isEmpty())
                .andExpect(jsonPath("$.sides[0].participantId").doesNotExist());
    }

    @Test
    void theSummaryOfAVenueIsAsTheContractSays() throws Exception {
        VenueId venue = new VenueId(UUID.randomUUID());
        use.venueSummary = Optional.of(new VenueSummary(venue, 3, 28, 400, T,
                List.of(new VenueGameTypeTotals(FOOSBALL, 3, 28, 400))));

        MvcResult result = call(get("/venues/{id}/summary", venue.value()).with(JwtTokens.display("acme")));

        assertContract("/venues/{venueId}/summary", result);
        mvc.perform(get("/venues/{id}/summary", venue.value()).with(JwtTokens.display("acme")))
                .andExpect(jsonPath("$.venueId").value(venue.value().toString()))
                .andExpect(jsonPath("$.totalPoints").value(28))
                .andExpect(jsonPath("$.averageDurationSeconds").value(400))
                .andExpect(jsonPath("$.byGameType[0].matches").value(3));
    }

    @Test
    void aVenueWithoutMatchesInTheFilterIsStillAsTheContractSays() throws Exception {
        VenueId venue = new VenueId(UUID.randomUUID());
        use.venueSummary = Optional.of(new VenueSummary(venue, 0, 0, 0, null, List.of()));

        MvcResult result = call(get("/venues/{id}/summary", venue.value()).with(JwtTokens.manager("acme")));

        assertContract("/venues/{venueId}/summary", result);
    }

    @Test
    void theErrorsAreAsTheContractSays() throws Exception {
        assertContract("/leaderboard", call(get("/leaderboard").with(JwtTokens.manager("acme")).param("limit", "0")));
        assertContract("/matches/{matchId}", call(get("/matches/{id}", ID).with(JwtTokens.manager("acme"))));
        assertContract("/leaderboard", call(get("/leaderboard").with(JwtTokens.playerWithoutPlayerId())));
        assertContract("/leaderboard", call(get("/leaderboard")));
    }

    @Test
    void theContractDeclaresTheStatusesTheCodeAnswers() {
        for (String path : List.of("/leaderboard", "/participants/{participantId}/summary",
                "/participants/{participantId}/matches", "/matches/{matchId}", "/venues/{venueId}/summary")) {
            for (int status : List.of(200, 400, 401, 403)) {
                assertThat(CONTRACT.declares(path, status)).as(path + " " + status).isTrue();
            }
        }
        for (String path : List.of("/participants/{participantId}/summary", "/participants/{participantId}/matches",
                "/matches/{matchId}", "/venues/{venueId}/summary")) {
            assertThat(CONTRACT.declares(path, 404)).as(path + " 404").isTrue();
        }
    }
}
