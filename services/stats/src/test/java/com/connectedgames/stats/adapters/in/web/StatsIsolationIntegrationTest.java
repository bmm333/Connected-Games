package com.connectedgames.stats.adapters.in.web;

import java.util.UUID;

import com.connectedgames.stats.Fixtures;
import com.connectedgames.stats.MosquittoTestConfiguration;
import com.connectedgames.stats.TestcontainersConfiguration;
import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static com.connectedgames.stats.MatchBuilder.match;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The whole way, token to database, with two organizations in the same tables: nobody reads what is not theirs. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, MosquittoTestConfiguration.class})
class StatsIsolationIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    MatchResultRepository repository;
    @Autowired
    JdbcTemplate jdbc;

    private final VenueId acmeVenue = new VenueId(UUID.randomUUID());
    private final VenueId globexVenue = new VenueId(UUID.randomUUID());
    private MatchResult acmeMatch;
    private MatchResult globexMatch;
    private MatchResult strangersMatch;

    @BeforeEach
    void twoOrganizations() {
        jdbc.update("delete from match_result");
        acmeMatch = save(match().tenant("acme").venue(acmeVenue).red("player-1", "Ada").blue("acme-2", "Bruno")
                .score(5, 3).points("RRBRBRBR").endedAt(Fixtures.END.minusSeconds(3600)).build());
        globexMatch = save(match().tenant("globex").venue(globexVenue).red("player-1", "Ada").blue("globex-2", "Carla")
                .score(2, 5).build());
        strangersMatch = save(match().tenant("globex").venue(globexVenue).red("globex-2", "Carla")
                .blue("globex-3", "Dario").score(5, 0).build());
    }

    private MatchResult save(MatchResult result) {
        repository.saveIfAbsent(result);
        return result;
    }

    // ---- managers and displays

    @Test
    void aManagerSeesOnlyItsOwnOrganization() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].participantId", containsInAnyOrder("player-1", "acme-2")));
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("globex")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].participantId", containsInAnyOrder("player-1", "globex-2", "globex-3")));
    }

    @Test
    void aManagerOfTwoOrganizationsSeesBoth() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("acme", "globex")))
                .andExpect(jsonPath("$[*].participantId", containsInAnyOrder("player-1", "acme-2", "globex-2", "globex-3")));
    }

    @Test
    void anOrganizationWithoutMatchesSeesAnEmptyLeaderboard() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.manager("initech")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void aManagerCountsOnlyTheMatchesOfItsOrganization() throws Exception {
        mvc.perform(get("/participants/player-1/summary").with(JwtTokens.manager("acme")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.wins").value(1));
        mvc.perform(get("/participants/player-1/summary").with(JwtTokens.manager("globex")))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.losses").value(1));
        mvc.perform(get("/participants/player-1/matches").with(JwtTokens.manager("acme")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].matchId").value(acmeMatch.id().value().toString()));
    }

    @Test
    void aManagerCannotSeeAParticipantOfAnotherOrganization() throws Exception {
        mvc.perform(get("/participants/globex-3/summary").with(JwtTokens.manager("acme"))).andExpect(status().isNotFound());
        mvc.perform(get("/participants/globex-3/matches").with(JwtTokens.manager("acme"))).andExpect(status().isNotFound());
    }

    @Test
    void aManagerCannotSeeAMatchOfAnotherOrganization() throws Exception {
        mvc.perform(get("/matches/{id}", strangersMatch.id().value()).with(JwtTokens.manager("acme")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/matches/{id}", strangersMatch.id().value()).with(JwtTokens.manager("globex")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(strangersMatch.id().value().toString()));
    }

    @Test
    void aManagerCannotSeeAVenueOfAnotherOrganization() throws Exception {
        mvc.perform(get("/venues/{id}/summary", globexVenue.value()).with(JwtTokens.manager("acme")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/venues/{id}/summary", acmeVenue.value()).with(JwtTokens.manager("acme")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.totalPoints").value(8));
    }

    @Test
    void aDisplayReadsTheOrganizationOfItsToken() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.display("acme")))
                .andExpect(jsonPath("$[*].participantId", containsInAnyOrder("player-1", "acme-2")));
        mvc.perform(get("/venues/{id}/summary", acmeVenue.value()).with(JwtTokens.display("acme")))
                .andExpect(status().isOk());
        mvc.perform(get("/venues/{id}/summary", globexVenue.value()).with(JwtTokens.display("acme")))
                .andExpect(status().isNotFound());
    }

    // ---- players

    @Test
    void aPlayerSeesItsOwnMatchesInEveryOrganization() throws Exception {
        mvc.perform(get("/participants/player-1/summary").with(JwtTokens.player("player-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").value(2))
                .andExpect(jsonPath("$.wins").value(1))
                .andExpect(jsonPath("$.losses").value(1));
        mvc.perform(get("/participants/player-1/matches").with(JwtTokens.player("player-1")))
                .andExpect(jsonPath("$[*].matchId", contains(
                        globexMatch.id().value().toString(), acmeMatch.id().value().toString())));
    }

    @Test
    void aGuestIsAPlayerToo() throws Exception {
        mvc.perform(get("/participants/player-1/summary").with(JwtTokens.guest("player-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").value(2));
    }

    @Test
    void aPlayerCannotReadAnotherParticipant() throws Exception {
        mvc.perform(get("/participants/acme-2/summary").with(JwtTokens.player("player-1"))).andExpect(status().isForbidden());
        mvc.perform(get("/participants/acme-2/matches").with(JwtTokens.player("player-1"))).andExpect(status().isForbidden());
    }

    @Test
    void aPlayerCannotReadALeaderboardOrAVenue() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.player("player-1"))).andExpect(status().isForbidden());
        mvc.perform(get("/venues/{id}/summary", acmeVenue.value()).with(JwtTokens.player("player-1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void aPlayerSeesTheMatchesItPlayedAndNoOthers() throws Exception {
        mvc.perform(get("/matches/{id}", globexMatch.id().value()).with(JwtTokens.player("player-1")))
                .andExpect(status().isOk());
        mvc.perform(get("/matches/{id}", strangersMatch.id().value()).with(JwtTokens.player("player-1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void aPlayerWhoNeverPlayedFindsNothing() throws Exception {
        mvc.perform(get("/participants/newcomer/summary").with(JwtTokens.player("newcomer")))
                .andExpect(status().isNotFound());
    }

    // ---- nobody

    @Test
    void withoutATokenNothingIsRead() throws Exception {
        mvc.perform(get("/leaderboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/matches/{id}", acmeMatch.id().value())).andExpect(status().isUnauthorized());
    }

    @Test
    void anAdminWithoutPlayerRoleReadsNothing() throws Exception {
        mvc.perform(get("/leaderboard").with(JwtTokens.admin("player-1"))).andExpect(status().isForbidden());
        mvc.perform(get("/participants/player-1/summary").with(JwtTokens.admin("player-1"))).andExpect(status().isForbidden());
    }
}
