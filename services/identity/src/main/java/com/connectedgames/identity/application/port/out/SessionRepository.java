package com.connectedgames.identity.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;

public interface SessionRepository{
    void save(Session session);
    Optional<Session> findById(SessionId id);
    List<Session> findBySubject(UUID subjectId);
}
