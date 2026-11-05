package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Row of the "session" table. */
@Entity
@Table(name = "session")
class SessionEntity {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private SessionKind kind;
    private UUID subjectId;
    private Instant startedAt;
    private Instant refreshExpiresAt;
    private Instant revokedAt;

    protected SessionEntity() {
        // required by JPA
    }

    static SessionEntity from(Session session) {
        SessionEntity row = new SessionEntity();
        row.id = session.id().value();
        row.kind = session.kind();
        row.subjectId = session.subjectId();
        row.startedAt = session.startedAt();
        row.refreshExpiresAt = session.refreshExpiresAt();
        row.revokedAt = session.revokedAt();
        return row;
    }

    Session toDomain() {
        return new Session(new SessionId(id), kind, subjectId, startedAt, refreshExpiresAt, revokedAt);
    }
}
