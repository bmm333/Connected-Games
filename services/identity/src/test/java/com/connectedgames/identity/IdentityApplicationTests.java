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
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the whole service on a real port with PostgreSQL and the demo users, and goes through the
 * contract as a client would: wiring, security, persistence and tokens together.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.locations=classpath:db/migration,classpath:db/demo")
class IdentityApplicationTests {

    private static final String MANAGER = """
            {"email":"gestore@belvedere.example","password":"password123"}""";

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Value("${local.server.port}")
    int port;

    @Test
    void aGuestRegistersAndKeepsItsPlayer() throws Exception {
        HttpResponse<String> guestResponse = send("POST", "/sessions/guest", "{\"nickname\":\"Bomber\"}", null);
        assertThat(guestResponse.statusCode()).isEqualTo(200);
        assertThat(guestResponse.headers().firstValue("Cache-Control")).hasValue("no-store");
        JsonNode guest = json.readTree(guestResponse.body());
        assertThat(guest.get("kind").asString()).isEqualTo("GUEST");
        assertThat(guest.has("user")).isFalse();

        HttpResponse<String> registered = send("POST", "/users", """
                {"email":"bomber@example.com","password":"password123","displayName":"Bomber Rossi"}""",
                guest.get("accessToken").asString());

        assertThat(registered.statusCode()).isEqualTo(201);
        JsonNode user = json.readTree(registered.body());
        assertThat(user.get("kind").asString()).isEqualTo("USER");
        assertThat(user.get("user").get("playerId").asString())
                .isEqualTo(guest.get("guest").get("playerId").asString());
        // the guest session ended with the registration
        assertThat(refresh(guest.get("refreshToken").asString()).statusCode()).isEqualTo(401);
    }

    @Test
    void aDemoManagerLogsInAndReadsItsProfile() throws Exception {
        JsonNode session = json.readTree(send("POST", "/sessions", MANAGER, null).body());

        HttpResponse<String> me = send("GET", "/users/me", null, session.get("accessToken").asString());

        assertThat(me.statusCode()).isEqualTo(200);
        JsonNode profile = json.readTree(me.body());
        assertThat(profile.get("email").asString()).isEqualTo("gestore@belvedere.example");
        assertThat(profile.get("roles").get(0).has("venueId")).isFalse();
        assertThat(profile.get("roles").get(1).get("role").asString()).isEqualTo("VENUE_MANAGER");
        assertThat(profile.get("roles").get(1).get("venueId").asString())
                .isEqualTo("b0000000-0000-4000-8000-000000000001");
    }

    @Test
    void theAccessTokenCarriesTheClaimsOfTheContract() throws Exception {
        JsonNode session = json.readTree(send("POST", "/sessions", MANAGER, null).body());
        String[] parts = session.get("accessToken").asString().split("\\.");
        JsonNode header = json.readTree(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
        JsonNode claims = json.readTree(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));

        assertThat(claims.get("iss").asString()).isEqualTo("connected-games-identity");
        assertThat(claims.get("aud").toString()).contains("connected-games");
        assertThat(claims.get("sub").asString()).isEqualTo("e0000000-0000-4000-8000-000000000001");
        assertThat(claims.get("sid").asString()).isEqualTo(session.get("id").asString());
        assertThat(claims.get("kind").asString()).isEqualTo("USER");
        assertThat(claims.get("exp").asLong() - claims.get("iat").asLong()).isEqualTo(900);

        JsonNode keys = json.readTree(send("GET", "/.well-known/jwks.json", null, null).body()).get("keys");
        assertThat(keys.get(0).get("kid").asString()).isEqualTo(header.get("kid").asString());
        // only the public half is published
        assertThat(keys.get(0).has("d")).isFalse();
    }

    @Test
    void refreshRotatesTheTokenAndDetectsReuse() throws Exception {
        String first = json.readTree(send("POST", "/sessions", MANAGER, null).body()).get("refreshToken").asString();

        HttpResponse<String> refreshed = refresh(first);
        assertThat(refreshed.statusCode()).isEqualTo(200);
        String second = json.readTree(refreshed.body()).get("refreshToken").asString();

        assertThat(refresh(first).statusCode()).isEqualTo(401);
        assertThat(refresh(second).statusCode()).isEqualTo(401);
    }

    @Test
    void anExpiredAccessTokenDoesNotBlockTheRefresh() throws Exception {
        String refreshToken = json.readTree(send("POST", "/sessions", MANAGER, null).body()).get("refreshToken").asString();

        HttpResponse<String> refreshed = send("POST", "/sessions/refresh",
                "{\"refreshToken\":\"" + refreshToken + "\"}", "not-a-valid-jwt");

        assertThat(refreshed.statusCode()).isEqualTo(200);
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        JsonNode session = json.readTree(send("POST", "/sessions", MANAGER, null).body());

        assertThat(send("DELETE", "/sessions/current", null, session.get("accessToken").asString()).statusCode())
                .isEqualTo(204);
        assertThat(refresh(session.get("refreshToken").asString()).statusCode()).isEqualTo(401);
    }

    @Test
    void errorsHaveTheStatusOfTheContract() throws Exception {
        HttpResponse<String> wrongPassword = send("POST", "/sessions",
                "{\"email\":\"gestore@belvedere.example\",\"password\":\"wrong-password\"}", null);
        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(wrongPassword.headers().firstValue("Content-Type")).hasValue("application/problem+json");

        assertThat(send("POST", "/users", """
                {"email":"giulia@example.com","password":"password123","displayName":"Giulia Bis"}""", null)
                .statusCode()).isEqualTo(409);
        assertThat(send("POST", "/users", """
                {"email":"short@example.com","password":"short","displayName":"Short"}""", null)
                .statusCode()).isEqualTo(400);
        assertThat(send("POST", "/users", """
                {"email":"not-an-email","password":"password123","displayName":"Nobody"}""", null)
                .statusCode()).isEqualTo(400);
        assertThat(send("POST", "/sessions/guest", "{\"nickname\":\"B\"}", null).statusCode()).isEqualTo(400);
        assertThat(refresh("never-issued").statusCode()).isEqualTo(401);
    }

    @Test
    void theProfileNeedsAUserToken() throws Exception {
        HttpResponse<String> anonymous = send("GET", "/users/me", null, null);
        assertThat(anonymous.statusCode()).isEqualTo(401);
        assertThat(anonymous.headers().firstValue("Content-Type")).hasValue("application/problem+json");

        String guestToken = json.readTree(send("POST", "/sessions/guest", "{\"nickname\":\"Bomber\"}", null).body())
                .get("accessToken").asString();
        assertThat(send("GET", "/users/me", null, guestToken).statusCode()).isEqualTo(403);
    }

    @Test
    void healthIsPublic() throws Exception {
        assertThat(send("GET", "/actuator/health", null, null).statusCode()).isEqualTo(200);
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
