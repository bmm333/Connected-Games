package com.connectedgames.identity.application.port.in;

import java.time.Instant;

import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;

public record IssuedSession(SessionId id,SessionKind kind,String accessToken,Instant accessTokenExpiresAt,
        String refreshToken,Instant refreshExpiresAt) {}
