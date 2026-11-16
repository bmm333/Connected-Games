package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.MatchEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class MatchEventPersistenceAdapter implements MatchEventRepository {

    private final MatchEventJpaRepository jpa;

    MatchEventPersistenceAdapter(MatchEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsById(EventId id) {
        return jpa.existsById(id.value());
    }

    @Override
    public boolean existsByMatchAndSeq(VenueId venueId, MatchId matchId, int seq) {
        return jpa.existsByVenueIdAndMatchIdAndSeq(venueId.value(), matchId.value(), seq);
    }

    @Override
    public void save(MatchEvent event) {
        jpa.save(MatchEventEntity.from(event));
    }

    @Override
    public List<MatchEvent> findByMatchId(VenueId venueId, MatchId matchId) {
        return jpa.findByVenueIdAndMatchIdOrderBySeqAsc(venueId.value(), matchId.value()).stream()
                .map(MatchEventEntity::toDomain)
                .toList();
    }
}
