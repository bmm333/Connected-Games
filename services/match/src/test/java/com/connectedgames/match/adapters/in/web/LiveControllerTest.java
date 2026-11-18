package com.connectedgames.match.adapters.in.web;

import com.connectedgames.match.application.port.out.MatchChangeNotifier;
import com.connectedgames.match.domain.TableId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.connectedgames.match.adapters.in.web.StubUseCases.FINISHED;
import static com.connectedgames.match.adapters.in.web.StubUseCases.MATCH;
import static com.connectedgames.match.adapters.in.web.StubUseCases.TABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/** The stream of a table: what is sent when it opens, when a match changes, and while nothing happens. */
@WebMvcTest(LiveController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StubUseCases.class, TableStreams.class})
@TestPropertySource(properties = "match.live.heartbeat=200ms")
class LiveControllerTest {

    @Autowired MockMvc mvc;
    @Autowired MatchChangeNotifier notifier;

    private MvcResult open(UUID tableId) throws Exception {
        return mvc.perform(get("/tables/{id}/live", tableId).accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();
    }

    private static String sent(MvcResult stream) {
        try {
            return stream.getResponse().getContentAsString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void waitFor(MvcResult stream, String text) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> sent(stream).contains(text));
    }

    private static long dataEvents(MvcResult stream) {
        return sent(stream).lines().filter(line -> line.startsWith("data:")).count();
    }

    @Test
    void sendsTheCurrentMatchAsSoonAsItOpensAsAnEventStream() throws Exception {
        MvcResult stream = open(TABLE.value());

        waitFor(stream, "\"id\":\"" + MATCH.value() + "\"");

        assertThat(stream.getResponse().getContentType()).startsWith("text/event-stream");
        assertThat(sent(stream)).contains("data:").contains("\"status\":\"IN_PROGRESS\"");
    }

    @Test
    void staysOpenAndSilentWhenTheTableHasNoMatchYet() throws Exception {
        MvcResult stream = open(UUID.randomUUID());

        waitFor(stream, "keep-alive"); // time has passed and the stream is still open

        assertThat(dataEvents(stream)).isZero();
    }

    @Test
    void sendsTheNewStateWhenAMatchOfTheTableChanges() throws Exception {
        MvcResult stream = open(TABLE.value());
        waitFor(stream, "IN_PROGRESS");

        notifier.matchChanged(TABLE, FINISHED);

        waitFor(stream, "\"status\":\"COMPLETED\"");
        assertThat(sent(stream)).contains("\"winnerSide\":\"RED\"");
    }

    @Test
    void doesNotSendTheChangesOfAnotherTable() throws Exception {
        MvcResult stream = open(TABLE.value());
        waitFor(stream, "IN_PROGRESS");

        notifier.matchChanged(new TableId(UUID.randomUUID()), FINISHED);
        waitFor(stream, "keep-alive");

        assertThat(dataEvents(stream)).isEqualTo(1);
    }

    @Test
    void everyoneWatchingTheTableGetsTheChange() throws Exception {
        MvcResult first = open(TABLE.value());
        MvcResult second = open(TABLE.value());
        waitFor(first, "IN_PROGRESS");
        waitFor(second, "IN_PROGRESS");

        notifier.matchChanged(TABLE, FINISHED);

        waitFor(first, "COMPLETED");
        waitFor(second, "COMPLETED");
    }

    @Test
    void aMatchThatCannotBeReadDoesNotBreakTheStream() throws Exception {
        MvcResult stream = open(TABLE.value());
        waitFor(stream, "IN_PROGRESS");

        notifier.matchChanged(TABLE, new com.connectedgames.match.domain.MatchId(UUID.randomUUID())); // not found
        notifier.matchChanged(TABLE, FINISHED);

        waitFor(stream, "COMPLETED");
    }
}
