package com.connectedgames.match.adapters.out.persistence;

import com.connectedgames.match.application.port.out.MatchCompleted;
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
import com.connectedgames.match.domain.match.MatchSide;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** How a MatchCompleted is kept in the outbox: the format of the table, not the one of the broker. */
final class OutboxPayloads {

    static final String MATCH_COMPLETED = "MatchCompleted";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    record SidePayload(Side side, String participantId, String displayName, int score) {}
    record CompletedPayload(UUID eventId, String tenantId, UUID venueId, UUID tableId, UUID matchId, UUID gameTypeId,
                            UUID tournamentMatchId, GameMode mode, int pointsToWin, Instant startedAt,
                            Instant endedAt, EndReason reason, Side winnerSide, List<SidePayload> sides) {}

    private OutboxPayloads() {
    }

    static String write(MatchCompleted m) {
        return JSON.writeValueAsString(new CompletedPayload(m.eventId().value(), m.tenantId().value(),
                m.venueId().value(), m.tableId().value(), m.matchId().value(), m.gameTypeId().value(),
                m.tournamentMatchId() == null ? null : m.tournamentMatchId().value(), m.mode(), m.pointsToWin(),
                m.startedAt(), m.endedAt(), m.reason(), m.winnerSide(),
                m.sides().stream().map(s -> new SidePayload(s.side(), s.participantId(), s.displayName(), s.score())).toList()));
    }

    static MatchCompleted read(String json) {
        CompletedPayload p = JSON.readValue(json, CompletedPayload.class);
        return new MatchCompleted(new EventId(p.eventId()), new TenantId(p.tenantId()), new VenueId(p.venueId()),
                new TableId(p.tableId()), new MatchId(p.matchId()), new GameTypeId(p.gameTypeId()),
                p.tournamentMatchId() == null ? null : new TournamentMatchId(p.tournamentMatchId()), p.mode(),
                p.pointsToWin(), p.startedAt(), p.endedAt(), p.reason(), p.winnerSide(),
                p.sides().stream().map(s -> new MatchSide(s.side(), s.participantId(), s.displayName(), s.score())).toList());
    }
}
