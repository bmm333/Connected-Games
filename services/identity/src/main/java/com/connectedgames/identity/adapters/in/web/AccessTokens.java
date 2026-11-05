package com.connectedgames.identity.adapters.in.web;

import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/** Reads the claims of our access tokens (contracts/openapi/identity.yaml, "Access token") */
final class AccessTokens {

    private AccessTokens() {
    }

    static SessionId sessionId(Jwt jwt) {
        return new SessionId(UUID.fromString(jwt.getClaimAsString("sid")));
    }

    static SessionKind kind(Jwt jwt) {
        return SessionKind.valueOf(jwt.getClaimAsString("kind"));
    }

    static UUID subject(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
