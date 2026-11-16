package com.connectedgames.match.application;

import java.util.List;

import com.connectedgames.match.application.port.in.GetMatchTimelineUseCase;
import com.connectedgames.match.application.port.out.MatchEventRepository;
import com.connectedgames.match.application.port.out.MatchRepository;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.event.MatchEvent;

public final class GetMatchTimelineService implements GetMatchTimelineUseCase {

    private final MatchRepository matches;
    private final MatchEventRepository events;

    public GetMatchTimelineService(MatchRepository matches, MatchEventRepository events) {
        this.matches = matches;
        this.events = events;
    }

    @Override
    public List<MatchEvent> getMatchTimeline(MatchId matchId) {
        // events saved before the start do not make a match yet: without the match there is no timeline
        matches.findById(matchId).orElseThrow(MatchNotFoundException::new);
        return events.findByMatchId(matchId);
    }
}
