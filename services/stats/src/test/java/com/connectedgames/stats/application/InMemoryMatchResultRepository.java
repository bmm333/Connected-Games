package com.connectedgames.stats.application;

import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.result.MatchResult;

import java.util.LinkedHashMap;
import java.util.Map;

class InMemoryMatchResultRepository implements MatchResultRepository {

    final Map<MatchId, MatchResult> saved = new LinkedHashMap<>();

    @Override
    public boolean saveIfAbsent(MatchResult result) {
        return saved.putIfAbsent(result.id(), result) == null;
    }
}
