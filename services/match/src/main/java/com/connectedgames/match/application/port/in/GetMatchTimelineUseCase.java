package com.connectedgames.match.application.port.in;

import java.util.List;

import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.event.MatchEvent;

public interface GetMatchTimelineUseCase {

    List<MatchEvent> getMatchTimeline(MatchId matchId);
}
