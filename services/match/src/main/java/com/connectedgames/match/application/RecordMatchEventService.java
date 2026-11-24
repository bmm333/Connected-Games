package com.connectedgames.match.application;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.application.port.out.MatchCompletedOutbox;
import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.match.Match;
import java.util.List;

public final class RecordMatchEventService implements RecordMatchEventUseCase {

    private final MatchEventRepository events;
    private final MatchRepository matches;
    private final MatchCompletedOutbox outbox;
    private final Supplier<UUID> ids;

    /** ids gives the id of each message written in the outbox. */
    public RecordMatchEventService(MatchEventRepository events, MatchRepository matches,
                                    MatchCompletedOutbox outbox,Supplier<UUID> ids) {
        this.events = events;
        this.matches = matches;
        this.outbox=outbox;
        this.ids=ids;
    }

    @Override
    public RecordResult record(MatchEvent event, EventTopic topic) {
        //event cannot be of other venue or table or match
        if(!topic.matches(event))
        {
            return new RecordResult.Rejected(event.id(),"The event does not match its topic");
        }
        //already exists
        if(events.existsById(event.id())){
            return new RecordResult.Duplicate(event.id());
        }
        //same seq, diff event: either one is wrong;
        if(events.existsByMatchAndSeq(event.venueId(), event.matchId(), event.seq()))
        {
            return new RecordResult.Rejected(event.id(),"seq " + event.seq() + " is already used in this match");
        }
        // match exists and belongs to another venue or table
        Optional<Match> curr=matches.findByIdForUpdate(event.matchId());
        if(curr.isPresent() && !belongsToTheSamePlace(curr.get(),event)){
            return new RecordResult.Rejected(event.id(), "The match belongs to another venue or table");
        }
        //save event, rebuild match from all its events.
        events.save(event);
        List<MatchEvent> all = events.findByMatchId(event.venueId(), event.matchId());
        Match.replay(all).ifPresent(match -> saveAndAnnounce(match, all));
        return new RecordResult.Accepted(event.id());
    }

    /**
     * A closed match gets its MatchCompleted in the same transaction. Every event of a closed match passes here,
     * and the outbox keeps only the first message of each match, so a late event does not send it twice.
     */
    private void saveAndAnnounce(Match match, List<MatchEvent> all)
    {
        matches.save(match);
        if(match.isClosed()){
            outbox.add(MatchCompleted.of(new EventId(ids.get()), match, match.pointsFrom(all)));
        }
    }
    private static boolean belongsToTheSamePlace(Match match,MatchEvent event)
    {
        return match.venueId().equals(event.venueId()) && match.tableId().equals(event.tableId());
    }
}













