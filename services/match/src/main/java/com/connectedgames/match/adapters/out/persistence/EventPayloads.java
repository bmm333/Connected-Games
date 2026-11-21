package com.connectedgames.match.adapters.out.persistence;

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
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


final class EventPayloads {

    static final String STARTED = "MatchStarted";
    static final String GOAL = "GoalScored";
    static final String ENDED = "MatchEnded";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    record StartedPayload(String tenantId, UUID gameTypeId, UUID tournamentMatchId, GameMode mode, int pointsToWin,
                          List<SidePayload> sides) {}
    record SidePayload(Side side, String participantId, String displayName) {}
    record GoalPayload(Side side) {}
    record EndedPayload(EndReason reason, List<ScorePayload> finalScore, Side winnerSide) {}
    record ScorePayload(Side side, int score) {}

    private EventPayloads() {
    }

    static String typeOf(MatchEvent event) {
        return switch (event) {
            case MatchStarted _ -> STARTED;
            case GoalScored _ -> GOAL;
            case MatchEnded _ -> ENDED;
        };
    }

    static String write(MatchEvent event) {
        Object payload = switch (event) {
            case MatchStarted e -> new StartedPayload(e.tenantId().value(), e.gameTypeId().value(),
                    e.tournamentMatchId() == null ? null : e.tournamentMatchId().value(), e.mode(), e.pointsToWin(),
                    e.sides().stream().map(s -> new SidePayload(s.side(), s.participantId(), s.displayName())).toList());
            case GoalScored e -> new GoalPayload(e.side());
            case MatchEnded e -> new EndedPayload(e.reason(),
                    e.finalScore().stream().map(s -> new ScorePayload(s.side(), s.score())).toList(), e.winnerSide());
        };
        return JSON.writeValueAsString(payload);
    }

    static MatchEvent read(String type, String json, EventId id, VenueId venueId, TableId tableId,
                           MatchId matchId, int seq, Instant occurredAt) {
        return switch (type) {
            case STARTED -> {
                StartedPayload p = JSON.readValue(json, StartedPayload.class);
                yield new MatchStarted(id, venueId, tableId, matchId, seq, occurredAt,
                        new TenantId(p.tenantId()), new GameTypeId(p.gameTypeId()),
                        p.tournamentMatchId() == null ? null : new TournamentMatchId(p.tournamentMatchId()),
                        p.mode(), p.pointsToWin(),
                        p.sides().stream().map(s -> new SideSetup(s.side(), s.participantId(), s.displayName())).toList());
            }
            case GOAL -> new GoalScored(id, venueId, tableId, matchId, seq, occurredAt,
                    JSON.readValue(json, GoalPayload.class).side());
            case ENDED -> {
                EndedPayload p = JSON.readValue(json, EndedPayload.class);
                yield new MatchEnded(id, venueId, tableId, matchId, seq, occurredAt, p.reason(),
                        p.finalScore().stream().map(s -> new SideScore(s.side(), s.score())).toList(), p.winnerSide());
            }
            default -> throw new IllegalStateException("Unknown event type in the log: " + type);
        };
    }
}
