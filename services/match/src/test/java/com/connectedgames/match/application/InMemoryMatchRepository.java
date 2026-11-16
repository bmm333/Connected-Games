package com.connectedgames.match.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchStatus;

class InMemoryMatchRepository implements MatchRepository {

    private final Map<MatchId, Match> matches = new HashMap<>();

    @Override
    public Optional<Match> findById(MatchId id) {
        return Optional.ofNullable(matches.get(id));
    }

    @Override
    public Optional<Match> findByIdForUpdate(MatchId id) {
        return findById(id);
    }

    @Override
    public Optional<Match> findCurrentByTable(TableId tableId) {
        return matches.values().stream()
                .filter(m -> m.tableId().equals(tableId) && m.status() == MatchStatus.IN_PROGRESS)
                .findFirst();
    }

    @Override
    public void save(Match match) {
        matches.put(match.id(), match);
    }

    int count() {
        return matches.size();
    }
}
