package com.connectedgames.identity.application.port.out;

import java.util.Optional;

import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;

public interface SessionRepository{
    void save(Session session);
    Optional<Session> findById(SessionId id);
}
