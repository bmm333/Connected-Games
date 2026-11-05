package com.connectedgames.identity.application.port.in;

import java.time.Instant;

import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;

/** What the client gets when a session starts or is refreshed; refreshToken is the only copy in clear */
public record IssuedSession(SessionId id,SessionKind kind,String accessToken,Instant accessTokenExpiresAt,
        String refreshToken,Instant refreshExpiresAt) {}
