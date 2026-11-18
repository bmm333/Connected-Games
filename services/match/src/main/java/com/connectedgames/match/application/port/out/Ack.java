package com.connectedgames.match.application.port.out;

import java.time.Instant;
import java.util.List;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.VenueId;

public record Ack(VenueId venueId,List<EventId> acknowledged,List<Rejection> rejected,Instant processedAt) {

}
