package com.connectedgames.match.application.port.out;

import java.util.List;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.event.MatchEvent;

public interface MatchEventRepository {
    boolean existsById(EventId id);
    boolean existsByMatchAndSeq(MatchId matchId, int seq);
    void save(MatchEvent event);
    List<MatchEvent> findByMatchId(MatchId matchId);
}
