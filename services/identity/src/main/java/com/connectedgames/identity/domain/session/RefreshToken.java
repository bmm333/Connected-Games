package com.connectedgames.identity.domain.session;

import java.time.Instant;
import java.util.Objects;

/** Single-use token that renews a session; only its SHA-256 hash is stored, never the token itself */
public record RefreshToken(String tokenHash,SessionId sessionId, Instant issuedAt,Instant usedAt)
{
    public RefreshToken{
        Objects.requireNonNull(tokenHash, "tokenHash");
        Objects.requireNonNull(sessionId,"sessionId");
        Objects.requireNonNull(issuedAt,"issuedAt");
        if(tokenHash.isBlank())
        {
            throw new IllegalArgumentException("Token hash cannot be blank");
        }
    }
    public static RefreshToken issue(String tokenHash,SessionId sessionId,Instant now)
    {
        return new RefreshToken(tokenHash, sessionId,now,null);
    }
    public boolean isUsed(){
        return usedAt!=null;
    }
    public RefreshToken used(Instant now)
    {
        if(isUsed())
        {
            throw new IllegalStateException("Refresh token already used");
        }
        return new RefreshToken(tokenHash, sessionId, issuedAt, now);
    }
}
