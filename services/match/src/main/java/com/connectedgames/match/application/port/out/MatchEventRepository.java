package com.connectedgames.match.application.port.out;

import java.util.List;

import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.MatchEvent;

/**
 * The log of every event received. A match is identified by venue and match id together, so an event
 * of another venue can never take the sequence number of a match that is not its own.
 */
public interface MatchEventRepository {
    boolean existsById(EventId id);
    boolean existsByMatchAndSeq(VenueId venueId, MatchId matchId, int seq);
    void save(MatchEvent event);
    /** The events of one match, ordered by seq. */
    List<MatchEvent> findByMatchId(VenueId venueId, MatchId matchId);
}
