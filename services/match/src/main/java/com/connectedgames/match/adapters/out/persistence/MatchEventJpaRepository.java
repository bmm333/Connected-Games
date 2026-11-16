package com.connectedgames.match.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface MatchEventJpaRepository extends JpaRepository<MatchEventEntity, UUID> {

    boolean existsByVenueIdAndMatchIdAndSeq(UUID venueId, UUID matchId, int seq);

    List<MatchEventEntity> findByVenueIdAndMatchIdOrderBySeqAsc(UUID venueId, UUID matchId);
}
