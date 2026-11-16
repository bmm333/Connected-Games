package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class MatchPersistenceAdapter implements MatchRepository {

    private final MatchJpaRepository jpa;

    MatchPersistenceAdapter(MatchJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Match> findById(MatchId id) {
        return jpa.findById(id.value()).map(MatchEntity::toDomain);
    }

    @Override
    public Optional<Match> findByIdForUpdate(MatchId id) {
        return jpa.findByIdForUpdate(id.value()).map(MatchEntity::toDomain);
    }

    @Override
    public Optional<Match> findCurrentByTable(TableId tableId) {
        return jpa.findFirstByTableIdAndStatusOrderByStartedAtDesc(tableId.value(), MatchStatus.IN_PROGRESS)
                .map(MatchEntity::toDomain);
    }

    @Override
    public void save(Match match) {
        jpa.save(MatchEntity.from(match));
    }
}
