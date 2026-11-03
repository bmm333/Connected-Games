package com.connectedgames.identity.domain.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Session(SessionId id,SessionKind kind,UUID subjectId,Instant startedAt, Instant refreshExpiresAt,Instant revokedAt) {

    public static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    public Session{
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(kind,"kind");
        Objects.requireNonNull(subjectId,"subjectId");
        Objects.requireNonNull(startedAt,"startedAt");
        Objects.requireNonNull(refreshExpiresAt,"refresExpiresAt");
        if(!refreshExpiresAt.isAfter(startedAt)){
            throw new IllegalArgumentException("Refresh cannot expire before starting");
        }
    }
    public static Session start(SessionId id,SessionKind kind,UUID subjectId,Instant now)
    {
        Instant exp=now.plus(kind.refreshLifetime());
        return new Session(id, kind, subjectId, now, exp , null);
    }
    public boolean isActive(Instant now)
    {
       return  revokedAt==null && now.isBefore(refreshExpiresAt);
    }
    public Session refreshed(Instant now)
    {
        if(!isActive(now))
        {
            throw new IllegalStateException("Session is not active: " + id);
        }
        if(!kind.extendedOnRefresh())
        {
            return this;    
        }
        return new Session(id,kind,subjectId,startedAt,now.plus(kind.refreshLifetime()),null);
    }
    public Session revoked(Instant now)
    {
        if(revokedAt!=null)
        {
            return this;
        }
        return new Session(id, kind, subjectId, startedAt, refreshExpiresAt,now);
    }

}
