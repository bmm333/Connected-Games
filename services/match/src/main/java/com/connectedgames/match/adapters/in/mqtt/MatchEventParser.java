package com.connectedgames.match.adapters.in.mqtt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedgames.match.domain.EventId;
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
import com.connectedgames.match.domain.event.SideSetup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

final class MatchEventParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Envelope(UUID eventId,String tenantId,UUID venueId,UUID tableId,UUID matchId,int seq,String type,Instant occurredAt,JsonNode payload)
    {}
    private record StartedPayload(UUID gameTypeId,UUID tournamentMatchId,GameMode mode,int pointsToWin,List<SidePayload> sides){}
    private record SidePayload(Side side,String participantId,String displayName){}
    private record GoalPayload(Side side){}
    private record EndedPayload(EndReason reason,List<ScorePayload> finalScore,Side winnerSide){}
    private record ScorePayload(Side side,int score){}
    MatchEvent parse(String json){
        Envelope e;
        try{
            e = JSON.readValue(json,Envelope.class);
        } catch(JacksonException ex){
            throw new UnreadableMessageException(idIn(json), "Not a valid event: " + ex.getOriginalMessage());
        }
        EventId id=e.eventId()==null ? null : new EventId(e.eventId());
        try{
            return build(e,id);
            //domain refuses what schema refuses no side, seq under 1 etc . exceptions are th ereason 
        } catch (JacksonException | IllegalArgumentException | NullPointerException ex){
            throw new UnreadableMessageException(id,"Not a valid event: " + reasonOf(ex));
        }
    }
    private static MatchEvent build(Envelope e, EventId id)
    {
        VenueId venue=new VenueId(e.venueId());
        TableId table=new TableId(e.tableId());
        MatchId match=new MatchId(e.matchId());
        if(e.type()==null||e.payload()==null)
        {
            throw new IllegalArgumentException("type and payload are required");
        }
        return switch (e.type()){
            case "MatchStarted" -> {
                StartedPayload p = JSON.treeToValue(e.payload(), StartedPayload.class);
                if(e.tenantId()==null || p.gameTypeId()==null)
                {
                    throw new IllegalArgumentException("tenantId and gameTypeId are required in a MatchStarted");
                }
                TournamentMatchId tournamentMatch=p.tournamentMatchId()==null ? null : new TournamentMatchId(p.tournamentMatchId());
                yield new MatchStarted(id, venue, table, match, e.seq(), e.occurredAt(),
                        new TenantId(e.tenantId()), new GameTypeId(p.gameTypeId()), tournamentMatch,
                        p.mode(), p.pointsToWin(),
                        p.sides().stream().map(s -> new SideSetup(s.side(), s.participantId(), s.displayName())).toList());
            }
            case "GoalScored" -> new GoalScored(id, venue, table, match, e.seq(), e.occurredAt(),
                    JSON.treeToValue(e.payload(), GoalPayload.class).side());
            case "MatchEnded" -> {
                EndedPayload p = JSON.treeToValue(e.payload(), EndedPayload.class);
                yield new MatchEnded(id, venue, table, match, e.seq(), e.occurredAt(), p.reason(),
                        p.finalScore().stream().map(s -> new SideScore(s.side(), s.score())).toList(), p.winnerSide());
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + e.type());
        };
    }
    /*BEST EFFORT: Even with a broken message we try to find an id to answer rejected instead of silence*/
    private static EventId idIn(String json)
    {
        try{
            JsonNode id=JSON.readTree(json).get("eventId");
            return id==null?null:new EventId(UUID.fromString(id.asString()));
        }catch (RuntimeException ex){
            return null;
        }
    }
    private static String reasonOf(RuntimeException ex){
        return ex instanceof JacksonException je ? je.getOriginalMessage() :
        String.valueOf(ex.getMessage());
    }
}












