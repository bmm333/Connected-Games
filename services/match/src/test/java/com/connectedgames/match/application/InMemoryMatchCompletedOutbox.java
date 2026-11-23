package com.connectedgames.match.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.domain.EventId;

class InMemoryMatchCompletedOutbox implements MatchCompletedOutbox {

    /** Everything ever added, in order, published or not. */
    final List<MatchCompleted> messages = new ArrayList<>();
    final Map<EventId, Instant> published = new LinkedHashMap<>();

    @Override
    public void add(MatchCompleted message) {
        if (messages.stream().noneMatch(m -> m.matchId().equals(message.matchId()))) {
            messages.add(message);
        }
    }

    @Override
    public List<MatchCompleted> unpublished(int limit) {
        return messages.stream().filter(m -> !published.containsKey(m.eventId())).limit(limit).toList();
    }

    @Override
    public void markPublished(EventId id, Instant at) {
        published.putIfAbsent(id, at);
    }
}
