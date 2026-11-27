package com.connectedgames.stats.adapters.in.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

/** Tokens shaped like the ones identity signs: contracts/openapi/identity.yaml, "Access token (JWT)". */
final class JwtTokens {

    private JwtTokens() {
    }

    static JwtRequestPostProcessor manager(String... organizations) {
        List<Map<String, String>> roles = new java.util.ArrayList<>();
        roles.add(Map.of("role", "PLAYER"));
        for (String organization : organizations) {
            roles.add(Map.of("role", "VENUE_MANAGER", "venueId", UUID.randomUUID().toString(),
                    "organizationId", organization));
        }
        return token("USER", UUID.randomUUID().toString(), roles, null, null);
    }

    static JwtRequestPostProcessor managerAndPlayer(String organization, String playerId) {
        return token("USER", UUID.randomUUID().toString(), List.of(Map.of("role", "PLAYER"),
                Map.of("role", "VENUE_MANAGER", "venueId", UUID.randomUUID().toString(), "organizationId", organization)),
                playerId, null);
    }

    static JwtRequestPostProcessor player(String playerId) {
        return token("USER", UUID.randomUUID().toString(), List.of(Map.of("role", "PLAYER")), playerId, null);
    }

    static JwtRequestPostProcessor playerWithoutPlayerId() {
        return token("USER", UUID.randomUUID().toString(), List.of(Map.of("role", "PLAYER")), null, null);
    }

    static JwtRequestPostProcessor admin(String playerId) {
        return token("USER", UUID.randomUUID().toString(), List.of(Map.of("role", "PLATFORM_ADMIN")), playerId, null);
    }

    static JwtRequestPostProcessor guest(String playerId) {
        return token("GUEST", playerId, List.of(Map.of("role", "PLAYER")), null, null);
    }

    static JwtRequestPostProcessor guestWithPlayerClaim(String subject, String playerId) {
        return token("GUEST", subject, List.of(Map.of("role", "PLAYER")), playerId, null);
    }

    static JwtRequestPostProcessor display(String organization) {
        return token("DEVICE", UUID.randomUUID().toString(), List.of(), null, organization);
    }

    static JwtRequestPostProcessor displayWithoutOrganization() {
        return token("DEVICE", UUID.randomUUID().toString(), List.of(), null, null);
    }

    static JwtRequestPostProcessor withoutKind() {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()));
    }

    static JwtRequestPostProcessor ofKind(String kind) {
        return token(kind, UUID.randomUUID().toString(), List.of(), null, null);
    }

    private static JwtRequestPostProcessor token(String kind, String subject, List<Map<String, String>> roles,
                                                 String playerId, String organizationId) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> {
            jwt.subject(subject).claim("kind", kind).claim("roles", roles);
            if (playerId != null) {
                jwt.claim("playerId", playerId);
            }
            if (organizationId != null) {
                jwt.claim("organizationId", organizationId);
                jwt.claim("venueId", UUID.randomUUID().toString());
                jwt.claim("tableId", UUID.randomUUID().toString());
            }
        });
    }
}
