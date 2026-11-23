package com.connectedgames.match.application.port.out;

import java.time.Instant;
import java.util.List;

import com.connectedgames.match.domain.EventId;

/** The messages for stats and tournament, kept in the database until the broker has them (ADR011). */
public interface MatchCompletedOutbox {

    /**
     * Writes the message in the transaction of the caller, so it exists if and only if the match is closed.
     * If the match already has its message, does nothing.
     */
    void add(MatchCompleted message);

    /** The messages not published yet, oldest first, at most limit. */
    List<MatchCompleted> unpublished(int limit);

    /** The broker has the message: it is not sent again. The row stays, for a backfill. */
    void markPublished(EventId id, Instant at);
}
