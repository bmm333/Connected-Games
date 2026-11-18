package com.connectedgames.match.adapters.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.connectedgames.match.adapters.in.web.WebResponses.MatchResponse;
import com.connectedgames.match.adapters.in.web.WebResponses.TimelineEntryResponse;
import com.connectedgames.match.application.port.in.GetCurrentMatchUseCase;
import com.connectedgames.match.application.port.in.GetMatchTimelineUseCase;
import com.connectedgames.match.application.port.in.GetMatchUseCase;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.TableId;

@RestController
class MatchController {
    private final GetCurrentMatchUseCase getCurrentMatch;
    private final GetMatchUseCase   getMatch;
    private final GetMatchTimelineUseCase getMatchTimeline;

    MatchController(GetCurrentMatchUseCase getCurrentMatch,GetMatchUseCase getMatch, GetMatchTimelineUseCase getMatchTimeline)
    {
        this.getCurrentMatch=getCurrentMatch;
        this.getMatch=getMatch;
        this.getMatchTimeline=getMatchTimeline;
    }
    @GetMapping("/tables/{tableId}/current-match")
    MatchResponse currentMatch(@PathVariable UUID tableId)
    {
        return MatchResponse.from(getCurrentMatch.getCurrentMatch(new TableId(tableId)));
    }
    @GetMapping("/matches/{matchId}")
    MatchResponse match(@PathVariable UUID matchId){
        return MatchResponse.from(getMatch.getMatch(new MatchId(matchId)));
    }
    @GetMapping("/matches/{matchId}/timeline")
    List<TimelineEntryResponse> timeline(@PathVariable UUID matchId)
    {
        return getMatchTimeline.getMatchTimeline(new MatchId(matchId)).stream().map(TimelineEntryResponse::from).toList();
    }
}
