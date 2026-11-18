package com.connectedgames.match.application.port.in;

import com.connectedgames.match.domain.EventId;

public sealed interface RecordResult {
    EventId eventId();
    record Accepted(EventId eventId) implements RecordResult {}
    record Duplicate(EventId eventId) implements RecordResult {}
    record Rejected(EventId eventId,String reason) implements RecordResult {}
}

