package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.session.RefreshToken;
import com.connectedgames.identity.domain.session.SessionId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Row of the "refresh_token" table: the hash is the key, the token itself is never stored. */
@Entity
@Table(name = "refresh_token")
class RefreshTokenEntity {

    @Id
    private String tokenHash;
    private UUID sessionId;
    private Instant issuedAt;
    private Instant usedAt;

    protected RefreshTokenEntity() {
        // required by JPA
    }

    static RefreshTokenEntity from(RefreshToken token) {
        RefreshTokenEntity row = new RefreshTokenEntity();
        row.tokenHash = token.tokenHash();
        row.sessionId = token.sessionId().value();
        row.issuedAt = token.issuedAt();
        row.usedAt = token.usedAt();
        return row;
    }

    RefreshToken toDomain() {
        return new RefreshToken(tokenHash, new SessionId(sessionId), issuedAt, usedAt);
    }
}
