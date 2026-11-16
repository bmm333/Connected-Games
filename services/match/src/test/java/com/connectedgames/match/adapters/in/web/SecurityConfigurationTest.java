package com.connectedgames.match.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.connectedgames.match.adapters.in.web.StubUseCases.MATCH;
import static com.connectedgames.match.adapters.in.web.StubUseCases.TABLE;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The access rules of contracts/openapi/match.yaml: reading is public, anything else needs a token. */
@WebMvcTest(MatchController.class)
@Import({SecurityConfiguration.class, StubUseCases.class})
class SecurityConfigurationTest {

    @Autowired MockMvc mvc;

    @Test
    void theCurrentMatchIsPublic() throws Exception {
        mvc.perform(get("/tables/{id}/current-match", TABLE.value())).andExpect(status().isOk());
    }

    @Test
    void aMatchAndItsTimelineArePublic() throws Exception {
        mvc.perform(get("/matches/{id}", MATCH.value())).andExpect(status().isOk());
        mvc.perform(get("/matches/{id}/timeline", MATCH.value())).andExpect(status().isOk());
    }

    @Test
    void onlyReadingIsPublic() throws Exception {
        mvc.perform(post("/matches/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void anythingElseNeedsAToken() throws Exception {
        mvc.perform(get("/venues/{id}/matches", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
