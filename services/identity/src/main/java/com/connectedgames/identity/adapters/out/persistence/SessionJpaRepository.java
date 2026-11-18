package com.connectedgames.identity.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {
    List<SessionEntity> findBySubjectId(UUID subjectId);
}
