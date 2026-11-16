package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.domain.match.MatchStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface MatchJpaRepository extends JpaRepository<MatchEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MatchEntity m where m.id = :id")
    Optional<MatchEntity> findByIdForUpdate(@Param("id") UUID id);

    Optional<MatchEntity> findFirstByTableIdAndStatusOrderByStartedAtDesc(UUID tableId, MatchStatus status);
}
