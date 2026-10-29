package com.connectedgames.catalog.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The access rules of contracts/openapi/catalog.yaml. jwt() stands in for a token signed by identity. */
@WebMvcTest(CatalogController.class)
@Import({SecurityConfiguration.class, StubUseCases.class})
class SecurityConfigurationTest {

    @Autowired MockMvc mvc;

    @Test
    void qrLookupIsPublic() throws Exception {
        mvc.perform(get("/tables/by-code/belvedere-t1"))
                .andExpect(status().isOk());
    }

    @Test
    void theRestOfTheCatalogNeedsAToken() throws Exception {
        mvc.perform(get("/venues"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void aValidTokenOpensTheCatalog() throws Exception {
        mvc.perform(get("/venues").with(jwt()))
                .andExpect(status().isOk());
    }
}
