package com.connectedgames.match.adapters.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    boolean existsByTypeAndMatchId(String type, UUID matchId);

    List<OutboxEventEntity> findByPublishedAtIsNullOrderBySeqAsc(Pageable page);

    /** Own transaction: the publisher calls it right after the broker took the message, outside any other. */
    @Transactional
    @Modifying
    @Query("update OutboxEventEntity e set e.publishedAt = :at where e.eventId = :eventId and e.publishedAt is null")
    int markPublished(@Param("eventId") UUID eventId, @Param("at") Instant at);
}
