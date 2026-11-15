package com.connectedgames.match.application.port.in;

import java.util.List;

import com.connectedgames.match.domain.VenueId;

public interface AcknowledgeEventUseCase {

    void acknowledge(VenueId venueId,List<RecordResult> results);
}
