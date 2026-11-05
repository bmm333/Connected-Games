package com.connectedgames.identity.adapters.out.security;

import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.application.port.out.AccessToken;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenIssuerTest {

    private static final UUID VENUE = UUID.randomUUID();
    private static final UUID ORGANIZATION = UUID.randomUUID();

    private final JwtConfiguration configuration = new JwtConfiguration();
    private final RSAKey key;
    private final JwtAccessTokenIssuer issuer;
    private final JwtDecoder decoder;

    JwtAccessTokenIssuerTest() throws Exception {
        key = configuration.signingKey("");
        issuer = new JwtAccessTokenIssuer(configuration.jwtEncoder(key));
        decoder = configuration.jwtDecoder(key);
    }

    private Session sessionStartedAt(Instant now) {
        return Session.start(new SessionId(UUID.randomUUID()), SessionKind.USER, UUID.randomUUID(), now);
    }

    @Test
    void carriesTheClaimsOfTheContract() {
        Instant now = Instant.now();
        Session session = sessionStartedAt(now);

        AccessToken token = issuer.issue(session, List.of(RoleAssignment.of(Role.PLAYER),
                RoleAssignment.venueManager(VENUE, ORGANIZATION)), now);
        Jwt jwt = decoder.decode(token.value());

        // "iss" is not a URL here, so it is read as a plain string
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("connected-games-identity");
        assertThat(jwt.getAudience()).containsExactly("connected-games");
        assertThat(jwt.getSubject()).isEqualTo(session.subjectId().toString());
        assertThat(jwt.getClaimAsString("sid")).isEqualTo(session.id().value().toString());
        assertThat(jwt.getClaimAsString("kind")).isEqualTo("USER");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.<List<Map<String, String>>>getClaim("roles")).containsExactly(
                Map.of("role", "PLAYER"),
                Map.of("role", "VENUE_MANAGER", "venueId", VENUE.toString(), "organizationId", ORGANIZATION.toString()));
    }

    @Test
    void lastsFifteenMinutesAndNamesItsKey() {
        Instant now = Instant.now();

        AccessToken token = issuer.issue(sessionStartedAt(now), List.of(RoleAssignment.of(Role.PLAYER)), now);
        Jwt jwt = decoder.decode(token.value());

        assertThat(token.expiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
        assertThat(jwt.getHeaders()).containsEntry("kid", key.getKeyID()).containsEntry("alg", "RS256");
    }

    @Test
    void anExpiredTokenIsRefused() {
        Instant anHourAgo = Instant.now().minus(Duration.ofHours(1));

        AccessToken token = issuer.issue(sessionStartedAt(anHourAgo), List.of(RoleAssignment.of(Role.PLAYER)), anHourAgo);

        assertThatThrownBy(() -> decoder.decode(token.value())).isInstanceOf(JwtValidationException.class);
    }

    @Test
    void aTokenSignedByAnotherKeyIsRefused() throws Exception {
        RSAKey otherKey = configuration.signingKey("");
        JwtAccessTokenIssuer other = new JwtAccessTokenIssuer(configuration.jwtEncoder(otherKey));
        Instant now = Instant.now();

        AccessToken token = other.issue(sessionStartedAt(now), List.of(RoleAssignment.of(Role.PLAYER)), now);

        assertThatThrownBy(() -> decoder.decode(token.value())).isInstanceOf(Exception.class);
    }
}
