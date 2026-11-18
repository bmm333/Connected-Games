package com.connectedgames.match.adapters.in.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.match.Match;
import com.connectedgames.match.domain.match.MatchSide;
import com.connectedgames.match.domain.match.MatchStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

final class WebResponses {
    private WebResponses() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MatchSideResponse(Side side,String participantId,String displayName, int score){
        static MatchSideResponse from(MatchSide s)
        {
            return new MatchSideResponse(s.side(), s.participantId(), s.displayName(), s.score());
        }
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MatchResponse(UUID id,UUID tableId, UUID venueId, MatchStatus status, GameMode mode, int pointsToWin,
                        List<MatchSideResponse> sides,Side winnerSide,Instant startedAt, Instant endedAt)
    {
        static MatchResponse from(Match m){
            return new MatchResponse(m.id().value(), m.tableId().value(), m.venueId().value(), m.status(), m.mode(),
                    m.pointsToWin(), m.sides().stream().map(MatchSideResponse::from).toList(), m.winnerSide(),
                    m.startedAt(), m.endedAt());
        }
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record TimelineEntryResponse(int seq,String type,Instant occurredAt,Side side)
    {
        static TimelineEntryResponse from(MatchEvent event)
        {
            return switch(event)
            {
                case MatchStarted e -> new TimelineEntryResponse(e.seq(), "MatchStarted", e.occurredAt(), null);
                case GoalScored e -> new TimelineEntryResponse(e.seq(), "GoalScored", e.occurredAt(), e.side());
                case MatchEnded e -> new TimelineEntryResponse(e.seq(), "MatchEnded", e.occurredAt(), null);
            };
        }
    }
}
