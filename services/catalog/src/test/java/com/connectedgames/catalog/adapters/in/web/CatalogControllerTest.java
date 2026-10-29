package com.connectedgames.catalog.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.connectedgames.catalog.adapters.in.web.StubUseCases.BELVEDERE;
import static com.connectedgames.catalog.adapters.in.web.StubUseCases.BROKEN_CODE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP mapping of the controller: paths, JSON shape and error codes. Security is off, see SecurityConfigurationTest. */
@WebMvcTest(CatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(StubUseCases.class)
class CatalogControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void resolvesATableByItsQrCode() throws Exception {
        mvc.perform(get("/tables/by-code/belvedere-t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.table.code").value("belvedere-t1"))
                .andExpect(jsonPath("$.table.status").value("FREE"))
                .andExpect(jsonPath("$.venue.name").value("Bar Belvedere"))
                .andExpect(jsonPath("$.venue.address").doesNotExist())
                .andExpect(jsonPath("$.gameType.mode").value("TEAM"));
    }

    @Test
    void malformedQrCodeIsNotFound() throws Exception {
        mvc.perform(get("/tables/by-code/x!"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unknownQrCodeIsAProblemDetail() throws Exception {
        mvc.perform(get("/tables/by-code/unknown-t9"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No table with code unknown-t9"));
    }

    @Test
    void inconsistentDataIsA500WithoutInternalDetails() throws Exception {
        mvc.perform(get("/tables/by-code/" + BROKEN_CODE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Inconsistent catalog data"));
    }

    @Test
    void passesTheCityFilterToTheUseCase() throws Exception {
        mvc.perform(get("/venues").param("city", "roma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bar Belvedere"))
                .andExpect(jsonPath("$[0].address").doesNotExist());

        mvc.perform(get("/venues").param("city", "Torino"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsTheVenueDetails() throws Exception {
        mvc.perform(get("/venues/{id}", BELVEDERE.id().value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Via del Belvedere 12"))
                .andExpect(jsonPath("$.organizationId").value(BELVEDERE.organizationId().value().toString()));
    }

    @Test
    void venueIdThatIsNotAUuidIsABadRequest() throws Exception {
        mvc.perform(get("/venues/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tablesOfAnUnknownVenueAreNotFound() throws Exception {
        mvc.perform(get("/venues/{id}/tables", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsTheTablesOfAVenueAndTheGameTypes() throws Exception {
        mvc.perform(get("/venues/{id}/tables", BELVEDERE.id().value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("belvedere-t1"));

        mvc.perform(get("/game-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantsPerSide").value(2))
                .andExpect(jsonPath("$[0].pointsToWin").value(10));
    }
}
