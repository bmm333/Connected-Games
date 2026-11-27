package com.connectedgames.identity.adapters.out.security;

import com.connectedgames.identity.application.port.out.AccessToken;
import com.connectedgames.identity.application.port.out.AccessTokenIssuer;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.RoleAssignment;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Implements AccessTokenIssuer: signs with RS256 the claims listed in contracts/openapi/identity.yaml */
@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

    static final String ISSUER = "connected-games-identity";
    static final String AUDIENCE = "connected-games";

    private final JwtEncoder encoder;

    JwtAccessTokenIssuer(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public AccessToken issue(Session session, PlayerId playerId, List<RoleAssignment> roles, Instant now) {
        return sign(baseClaims(session, now)
                .claim("playerId", playerId.value().toString())
                .claim("roles", roles.stream().map(JwtAccessTokenIssuer::roleClaim).toList()), now);
    }

    @Override
    public AccessToken issueForDevice(Session session, Device device, Instant now) {
        return sign(baseClaims(session, now)
                .claim("roles", List.of())
                .claim("venueId", device.venueId().toString())
                .claim("tableId", device.tableId().toString())
                .claim("organizationId", device.organizationId().toString()), now);
    }

    private static JwtClaimsSet.Builder baseClaims(Session session, Instant now) {
        return JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .subject(session.subjectId().toString())
                .claim("sid", session.id().value().toString())
                .claim("kind", session.kind().name())
                .issuedAt(now)
                .expiresAt(now.plus(Session.ACCESS_TOKEN_LIFETIME))
                .id(UUID.randomUUID().toString());
    }

    private AccessToken sign(JwtClaimsSet.Builder claims, Instant now) {
        // the encoder adds the kid of the signing key to the header
        String value = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims.build())).getTokenValue();
        return new AccessToken(value, now.plus(Session.ACCESS_TOKEN_LIFETIME));
    }

    /** One role as in the contract: {"role": "VENUE_MANAGER", "venueId": "...", "organizationId": "..."} */
    private static Map<String, String> roleClaim(RoleAssignment assignment) {
        Map<String, String> claim = new LinkedHashMap<>();
        claim.put("role", assignment.role().name());
        if (assignment.venueId() != null) {
            claim.put("venueId", assignment.venueId().toString());
        }
        if (assignment.organizationId() != null) {
            claim.put("organizationId", assignment.organizationId().toString());
        }
        return claim;
    }
}
