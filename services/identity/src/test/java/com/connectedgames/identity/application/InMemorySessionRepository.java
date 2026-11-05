package com.connectedgames.identity.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;

class InMemorySessionRepository implements SessionRepository {
    private final Map<SessionId, Session> sessions = new HashMap<>();

    @Override
    public void save(Session session)
    {
        sessions.put(session.id(), session);
    }

    @Override
    public Optional<Session> findById(SessionId id)
    {
        return Optional.ofNullable(sessions.get(id));
    }

    int count() { return sessions.size(); }
}
