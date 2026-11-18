package com.connectedgames.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/** The pairing of a display with a table, through the contract: manager, code, display, refresh, replacement. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.locations=classpath:db/migration,classpath:db/demo")
class DevicePairingIntegrationTest {

    /** The Bar Belvedere of the demo data: its manager is gestore@belvedere.example */
    private static final String BELVEDERE = "b0000000-0000-4000-8000-000000000001";

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Value("${local.server.port}")
    int port;

    private String loginAs(String email) throws Exception {
        HttpResponse<String> login = send("POST", "/sessions",
                "{\"email\":\"" + email + "\",\"password\":\"password123\"}", null);
        return json.readTree(login.body()).get("accessToken").asString();
    }

    private String managerToken() throws Exception {
        return loginAs("gestore@belvedere.example");
    }

    private HttpResponse<String> createCode(String token, String venue, UUID table) throws Exception {
        return send("POST", "/devices/pairing-codes",
                "{\"venueId\":\"" + venue + "\",\"tableId\":\"" + table + "\",\"surface\":\"KIOSK\"}", token);
    }

    private String newCode(UUID table) throws Exception {
        HttpResponse<String> created = createCode(managerToken(), BELVEDERE, table);
        assertThat(created.statusCode()).isEqualTo(201);
        return json.readTree(created.body()).get("code").asString();
    }

    private HttpResponse<String> pair(String code) throws Exception {
        return send("POST", "/devices/pair", "{\"pairingCode\":\"" + code + "\",\"surface\":\"KIOSK\"}", null);
    }

    @Test
    void aManagerCreatesACodeThatWorksForTenMinutesAndIsNotCached() throws Exception {
        HttpResponse<String> created = createCode(managerToken(), BELVEDERE, UUID.randomUUID());

        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.headers().firstValue("Cache-Control")).hasValue("no-store");
        JsonNode body = json.readTree(created.body());
        assertThat(body.get("code").asString()).matches("[A-Z0-9]{6}");
        assertThat(java.time.Instant.parse(body.get("expiresAt").asString()))
                .isBetween(java.time.Instant.now().plusSeconds(9 * 60), java.time.Instant.now().plusSeconds(10 * 60 + 5));
    }

    @Test
    void aDisplayPairsWithTheCodeAndGetsATokenForItsTable() throws Exception {
        UUID table = UUID.randomUUID();
        String code = newCode(table);

        HttpResponse<String> paired = pair(code);

        assertThat(paired.statusCode()).isEqualTo(200);
        assertThat(paired.headers().firstValue("Cache-Control")).hasValue("no-store");
        JsonNode session = json.readTree(paired.body());
        assertThat(session.get("kind").asString()).isEqualTo("DEVICE");
        JsonNode device = session.get("device");
        assertThat(device.get("venueId").asString()).isEqualTo(BELVEDERE);
        assertThat(device.get("tableId").asString()).isEqualTo(table.toString());
        assertThat(device.get("surface").asString()).isEqualTo("KIOSK");
        assertThat(session.has("user")).isFalse();
        assertThat(session.has("guest")).isFalse();

        JsonNode claims = claimsOf(session.get("accessToken").asString());
        assertThat(claims.get("kind").asString()).isEqualTo("DEVICE");
        assertThat(claims.get("sub").asString()).isEqualTo(device.get("id").asString());
        assertThat(claims.get("sid").asString()).isEqualTo(session.get("id").asString());
        assertThat(claims.get("roles")).isEmpty();
    }

    @Test
    void theCodeIsConsumedByThePairing() throws Exception {
        String code = newCode(UUID.randomUUID());
        assertThat(pair(code).statusCode()).isEqualTo(200);

        HttpResponse<String> again = pair(code);

        assertThat(again.statusCode()).isEqualTo(404);
        assertThat(again.headers().firstValue("Content-Type")).hasValue("application/problem+json");
    }

    @Test
    void anUnknownCodeIs404AndAMalformedOneIs400() throws Exception {
        assertThat(pair("ZZZZZZ").statusCode()).isEqualTo(404);
        assertThat(pair("abc").statusCode()).isEqualTo(400);
        assertThat(send("POST", "/devices/pair", "{\"pairingCode\":\"K7F2QX\",\"surface\":\"TV\"}", null)
                .statusCode()).isEqualTo(400);
    }

    @Test
    void pairingIgnoresATokenThatIsNotValid() throws Exception {
        String code = newCode(UUID.randomUUID());

        HttpResponse<String> paired = send("POST", "/devices/pair",
                "{\"pairingCode\":\"" + code + "\",\"surface\":\"KIOSK\"}", "an-expired-or-garbage-token");

        assertThat(paired.statusCode()).isEqualTo(200);
    }

    @Test
    void onlyTheManagerOfTheVenueCanCreateACode() throws Exception {
        UUID table = UUID.randomUUID();

        assertThat(createCode(null, BELVEDERE, table).statusCode()).isEqualTo(401);
        assertThat(createCode(loginAs("giulia@example.com"), BELVEDERE, table).statusCode()).isEqualTo(403);
        assertThat(createCode(loginAs("admin@connectedgames.example"), BELVEDERE, table).statusCode()).isEqualTo(403);
        assertThat(createCode(managerToken(), UUID.randomUUID().toString(), table).statusCode()).isEqualTo(403);
        String guest = json.readTree(send("POST", "/sessions/guest", "{\"nickname\":\"Bomber\"}", null).body())
                .get("accessToken").asString();
        assertThat(createCode(guest, BELVEDERE, table).statusCode()).isEqualTo(403);
    }

    @Test
    void aDisplayCannotCreateCodesForOtherDisplays() throws Exception {
        UUID table = UUID.randomUUID();
        String display = json.readTree(pair(newCode(table)).body()).get("accessToken").asString();

        assertThat(createCode(display, BELVEDERE, table).statusCode()).isEqualTo(403);
    }

    @Test
    void theDisplayRefreshesItsSession() throws Exception {
        JsonNode session = json.readTree(pair(newCode(UUID.randomUUID())).body());

        HttpResponse<String> refreshed = refresh(session.get("refreshToken").asString());

        assertThat(refreshed.statusCode()).isEqualTo(200);
        JsonNode next = json.readTree(refreshed.body());
        assertThat(next.get("kind").asString()).isEqualTo("DEVICE");
        assertThat(next.get("device").get("id").asString()).isEqualTo(session.get("device").get("id").asString());
        assertThat(next.get("refreshToken").asString()).isNotEqualTo(session.get("refreshToken").asString());
    }

    @Test
    void aNewDisplayReplacesTheOldOneOfTheTable() throws Exception {
        UUID table = UUID.randomUUID();
        JsonNode oldDisplay = json.readTree(pair(newCode(table)).body());
        JsonNode newDisplay = json.readTree(pair(newCode(table)).body());

        assertThat(refresh(oldDisplay.get("refreshToken").asString()).statusCode()).isEqualTo(401);
        assertThat(refresh(newDisplay.get("refreshToken").asString()).statusCode()).isEqualTo(200);
        assertThat(newDisplay.get("device").get("id").asString())
                .isNotEqualTo(oldDisplay.get("device").get("id").asString());
    }

    @Test
    void twoDisplaysWithTheSameCodeAtTheSameTimeOnlyOneWins() throws Exception {
        String code = newCode(UUID.randomUUID());
        int displays = 6;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(displays);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < displays; i++) {
                Callable<Integer> pairing = () -> {
                    start.await();
                    return pair(code).statusCode();
                };
                results.add(pool.submit(pairing));
            }
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> result : results) {
                statuses.add(result.get());
            }

            assertThat(statuses).filteredOn(status -> status == 200).hasSize(1);
            assertThat(statuses).filteredOn(status -> status == 404).hasSize(displays - 1);
        } finally {
            pool.shutdownNow();
        }
    }

    private JsonNode claimsOf(String accessToken) {
        String payload = accessToken.split("\\.")[1];
        return json.readTree(new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8));
    }

    private HttpResponse<String> refresh(String refreshToken) throws IOException, InterruptedException {
        return send("POST", "/sessions/refresh", "{\"refreshToken\":\"" + refreshToken + "\"}", null);
    }

    private HttpResponse<String> send(String method, String path, String body, String accessToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/identity" + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        if (accessToken != null) {
            request.header("Authorization", "Bearer " + accessToken);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
