package com.connectedgames.match.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.connectedgames.match.adapters.in.web.StubUseCases.FINISHED;
import static com.connectedgames.match.adapters.in.web.StubUseCases.IN_PROGRESS;
import static com.connectedgames.match.adapters.in.web.StubUseCases.MATCH;
import static com.connectedgames.match.adapters.in.web.StubUseCases.TABLE;
import static com.connectedgames.match.adapters.in.web.StubUseCases.VENUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP mapping of the controller: paths, JSON shape and error codes. Security is off, see SecurityConfigurationTest. */
@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(StubUseCases.class)
class MatchControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void returnsTheCurrentMatchOfATable() throws Exception {
        mvc.perform(get("/tables/{id}/current-match", TABLE.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MATCH.value().toString()))
                .andExpect(jsonPath("$.tableId").value(TABLE.value().toString()))
                .andExpect(jsonPath("$.venueId").value(VENUE.value().toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.mode").value("TEAM"))
                .andExpect(jsonPath("$.pointsToWin").value(5))
                .andExpect(jsonPath("$.startedAt").value("2026-11-16T20:00:00Z"));
    }

    @Test
    void showsBothSidesWithTheirScore() throws Exception {
        mvc.perform(get("/matches/{id}", MATCH.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sides.length()").value(2))
                .andExpect(jsonPath("$.sides[0].side").value("RED"))
                .andExpect(jsonPath("$.sides[0].displayName").value("Red"))
                .andExpect(jsonPath("$.sides[0].score").value(1))
                .andExpect(jsonPath("$.sides[1].side").value("BLUE"))
                .andExpect(jsonPath("$.sides[1].participantId").value("team-7"))
                .andExpect(jsonPath("$.sides[1].score").value(0));
    }

    @Test
    void leavesOutWhatIsAbsentInsteadOfSendingNull() throws Exception {
        mvc.perform(get("/matches/{id}", MATCH.value()))
                .andExpect(jsonPath("$.sides[0].participantId").doesNotExist())
                .andExpect(jsonPath("$.winnerSide").doesNotExist())
                .andExpect(jsonPath("$.endedAt").doesNotExist());
    }

    @Test
    void aFinishedMatchHasItsWinnerAndItsEnd() throws Exception {
        mvc.perform(get("/matches/{id}", FINISHED.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.winnerSide").value("RED"))
                .andExpect(jsonPath("$.endedAt").value("2026-11-16T20:10:00Z"))
                .andExpect(jsonPath("$.sides[0].score").value(5))
                .andExpect(jsonPath("$.sides[1].score").value(3));
    }

    @Test
    void anEmptyTableIsAProblemDetail() throws Exception {
        mvc.perform(get("/tables/{id}/current-match", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Match not found"));
    }

    @Test
    void anUnknownMatchIsAProblemDetail() throws Exception {
        mvc.perform(get("/matches/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void theTimelineListsTheEventsInOrder() throws Exception {
        mvc.perform(get("/matches/{id}/timeline", MATCH.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].seq").value(1))
                .andExpect(jsonPath("$[0].type").value("MatchStarted"))
                .andExpect(jsonPath("$[0].side").doesNotExist())
                .andExpect(jsonPath("$[1].seq").value(2))
                .andExpect(jsonPath("$[1].type").value("GoalScored"))
                .andExpect(jsonPath("$[1].side").value("RED"))
                .andExpect(jsonPath("$[1].occurredAt").value("2026-11-16T20:01:00Z"));
    }

    @Test
    void theTimelineOfAnUnknownMatchIsNotFound() throws Exception {
        mvc.perform(get("/matches/{id}/timeline", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void anIdThatIsNotAUuidIsABadRequest() throws Exception {
        mvc.perform(get("/matches/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
