package com.connectedgames.match.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.event.MatchEvent;

class InMemoryMatchEventRepository implements MatchEventRepository {

    private final List<MatchEvent> saved = new ArrayList<>();

    @Override
    public boolean existsById(EventId id) {
        return saved.stream().anyMatch(e -> e.id().equals(id));
    }

    @Override
    public boolean existsByMatchAndSeq(MatchId matchId, int seq) {
        return saved.stream().anyMatch(e -> e.matchId().equals(matchId) && e.seq() == seq);
    }

    @Override
    public void save(MatchEvent event) {
        saved.add(event);
    }

    @Override
    public List<MatchEvent> findByMatchId(MatchId matchId) {
        return saved.stream()
                .filter(e -> e.matchId().equals(matchId))
                .sorted(Comparator.comparingInt(MatchEvent::seq))
                .toList();
    }

    int count() {
        return saved.size();
    }
}
