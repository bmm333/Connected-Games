package com.connectedgames.match.application.port.in;

import com.connectedgames.match.domain.event.MatchEvent;

public interface RecordMatchEventUseCase {

    RecordResult record(MatchEvent event,EventTopic topic);
}
