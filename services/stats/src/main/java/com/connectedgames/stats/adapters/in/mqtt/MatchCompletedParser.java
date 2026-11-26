package com.connectedgames.stats.adapters.in.mqtt;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.connectedgames.stats.domain.EndReason;
import com.connectedgames.stats.domain.GameMode;
import com.connectedgames.stats.domain.GameTypeId;
import com.connectedgames.stats.domain.MatchId;
import com.connectedgames.stats.domain.MessageId;
import com.connectedgames.stats.domain.Side;
import com.connectedgames.stats.domain.TableId;
import com.connectedgames.stats.domain.TenantId;
import com.connectedgames.stats.domain.TournamentMatchId;
import com.connectedgames.stats.domain.VenueId;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.result.ScoredPoint;
import com.connectedgames.stats.domain.result.SideResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**Reads the message of platform/matches/completed 
 * Fields it dose not know are ignored so the match service can add some without breaking stats.
 * */
final class MatchCompletedParser {
    
    private static final JsonMapper JSON=JsonMapper.builder().build();
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(UUID eventId, String tenantId,UUID venueId,UUID tableId, UUID matchId, UUID gameTypeId,
    UUID tournamentMatchId,GameMode mode,int pointsToWin,Instant startedAt,Instant endedAt,EndReason reason,Side winnerSide,
    List<SidePayload> sides,List<PointPayload> points) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SidePayload(Side side, String participantId, String displayName, int score) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PointPayload(int seq, Side side, Instant occurredAt) {}

    MatchResult parse(String json) {
        Message m;
        try {
            m = JSON.readValue(json, Message.class);
        } catch (JacksonException e) {
            throw new UnreadableMessageException("Not a valid MatchCompleted: " + e.getOriginalMessage());
        }
        try {
            return build(m);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new UnreadableMessageException("Not a valid MatchCompleted: " + e.getMessage());
        }
    }

    private static MatchResult build(Message m) {
        Objects.requireNonNull(m.sides(), "sides");
        return new MatchResult(new MatchId(m.matchId()), new MessageId(m.eventId()), new TenantId(m.tenantId()),
                new VenueId(m.venueId()), new TableId(m.tableId()), new GameTypeId(m.gameTypeId()),
                m.tournamentMatchId() == null ? null : new TournamentMatchId(m.tournamentMatchId()),
                m.mode(), m.pointsToWin(), m.reason(), m.winnerSide(), m.startedAt(), m.endedAt(),
                m.sides().stream().map(s -> new SideResult(s.side(), s.participantId(), s.displayName(), s.score())).toList(),
                m.points() == null ? List.of()
                        : m.points().stream().map(p -> new ScoredPoint(p.seq(), p.side(), p.occurredAt())).toList());
    }
}
