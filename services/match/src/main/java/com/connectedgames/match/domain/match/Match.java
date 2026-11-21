package com.connectedgames.match.domain.match;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.GameTypeId;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.TenantId;
import com.connectedgames.match.domain.TournamentMatchId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.event.GoalScored;
import com.connectedgames.match.domain.event.MatchEnded;
import com.connectedgames.match.domain.event.MatchEvent;
import com.connectedgames.match.domain.event.MatchStarted;
import com.connectedgames.match.domain.event.SideScore;

/**
 * A match as the cloud sees it: always rebuilt from the events received, so the order of arrival does not matter.
 * tenantId, gameTypeId and tournamentMatchId come from MatchStarted and never change. tournamentMatchId is null
 * for a friendly or offline match.
 */
public record Match(MatchId id, VenueId venueId, TableId tableId, TenantId tenantId, GameTypeId gameTypeId,
                    TournamentMatchId tournamentMatchId, MatchStatus status,
                    GameMode mode, int pointsToWin, List<MatchSide> sides,
                    Side winnerSide, Instant startedAt, Instant endedAt) {

    /** Rebuilds the match from what arrived, in any order. */
    public static Optional<Match> replay(Collection<MatchEvent> events) {
        List<MatchEvent> ordered=events.stream()
            .sorted(Comparator.comparingInt(MatchEvent::seq)).toList();
        Optional<MatchStarted> started= ordered.stream().filter(MatchStarted.class::isInstance).map(MatchStarted.class::cast).findFirst();
        if(started.isEmpty()) return Optional.empty();
        Match match = start(started.get());
        for(MatchEvent event: ordered) match=match.apply(event);
        return Optional.of(match);
    }

    static Match start(MatchStarted event) {
        List<MatchSide> sides=event.sides().stream()
                    .map(s -> new MatchSide(s.side(),s.participantId(),s.displayName(),0))
                    .toList();
        return new Match(event.matchId(),event.venueId(),event.tableId(),event.tenantId(),event.gameTypeId(),
                        event.tournamentMatchId(),MatchStatus.IN_PROGRESS,
                        event.mode(),event.pointsToWin(),sides,null,event.occurredAt(),null);
    }

    Match apply(MatchEvent event) {
        if(!venueId.equals(event.venueId()) || !tableId.equals(event.tableId()))
        {
            return this;
        }
        return switch (event) { // sealed: no default
            case MatchStarted e -> this; // a second start does not change anything
            case GoalScored e -> status == MatchStatus.IN_PROGRESS ? withGoal(e.side()) : this; // +1 to the side, only if IN_PROGRESS
            case MatchEnded e -> status == MatchStatus.IN_PROGRESS ? ended(e) : this; // status from reason, scores from finalScore
        };
    }
    
    private Match withGoal(Side side)
    {
        List<MatchSide> updated = sides.stream().map(s->s.side()==side ? 
                             new MatchSide(s.side(), s.participantId(), s.displayName(), s.score()+1) : s)
                        .toList();
        return new Match(id,venueId,tableId,tenantId,gameTypeId,tournamentMatchId,status,mode,pointsToWin,updated,winnerSide,startedAt,endedAt);
    }
    
    private Match ended(MatchEnded event)
    {
        MatchStatus finalStatus= event.reason() == EndReason.CANCELLED ? MatchStatus.CANCELLED : MatchStatus.COMPLETED;
        List<MatchSide> scored= sides.stream()
            .map(s -> new MatchSide(s.side(),s.participantId(),s.displayName(),scoreIn(event.finalScore(),s.side())))
            .toList();
        return new Match(id,venueId,tableId,tenantId,gameTypeId,tournamentMatchId,finalStatus,mode,pointsToWin,scored,event.winnerSide(),startedAt,event.occurredAt());
    }
    public boolean isClosed(){
        return status!=MatchStatus.IN_PROGRESS;
    }
    public int scoreOf(Side side) {
        return sides.stream()
            .filter(s -> s.side() == side)
            .mapToInt(MatchSide::score)
            .findFirst()
            .orElseThrow();
    }

    private static int scoreIn(List<SideScore> scores, Side side) {
        return scores.stream()
            .filter(s -> s.side() == side)
            .mapToInt(SideScore::score)
            .findFirst()
            .orElseThrow();
    }
}
