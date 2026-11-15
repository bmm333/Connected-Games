package com.connectedgames.match.application;

import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.event.MatchEvent;

public final class RecordMatchEventService implements RecordMatchEventUseCase {

    private final MatchEventRepository events;
    private final MatchRepository matches;

    public RecordMatchEventService(MatchEventRepository events, MatchRepository matches) {
        this.events = events;
        this.matches = matches;
    }

    @Override
    public RecordResult record(MatchEvent event, EventTopic topic) {
        throw new UnsupportedOperationException("TODO");
    }
}
