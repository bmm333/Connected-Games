package com.connectedgames.match.application;

import java.util.Optional;

import com.connectedgames.match.application.port.in.EventTopic;
import com.connectedgames.match.application.port.in.RecordMatchEventUseCase;
import com.connectedgames.match.application.port.in.RecordResult;
import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.match.Match;

public final class RecordMatchEventService implements RecordMatchEventUseCase {

    private final MatchEventRepository events;
    private final MatchRepository matches;

    public RecordMatchEventService(MatchEventRepository events, MatchRepository matches) {
        this.events = events;
        this.matches = matches;
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
        if(events.existsByMatchAndSeq(event.matchId(), event.seq()))
        {
            return new RecordResult.Rejected(event.id(),"seq " + event.seq() + " is already used in this match");
        }
        // match exists and belongs to another venue or table
        Optional<Match> curr=matches.findById(event.matchId());
        if(curr.isPresent() && !belongsToTheSamePlace(curr.get(),event)){
            return new RecordResult.Rejected(event.id(), "The match belongs to another venue or table");
        }
        //save event, rebuild match from all its events.
        events.save(event);
        Match.replay(events.findByMatchId(event.matchId())).ifPresent(matches::save);
        return new RecordResult.Accepted(event.id());
    }
    private static boolean belongsToTheSamePlace(Match match,MatchEvent event)
    {
        return match.venueId().equals(event.venueId()) && match.tableId().equals(event.tableId());
    }
}













