package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.application.port.out.SessionRepository;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implements the SessionRepository port with JPA. */
@Component
class SessionPersistenceAdapter implements SessionRepository {

    private final SessionJpaRepository jpa;

    SessionPersistenceAdapter(SessionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Session session) {
        jpa.save(SessionEntity.from(session));
    }

    @Override
    public Optional<Session> findById(SessionId id) {
        return jpa.findById(id.value()).map(SessionEntity::toDomain);
    }
}
