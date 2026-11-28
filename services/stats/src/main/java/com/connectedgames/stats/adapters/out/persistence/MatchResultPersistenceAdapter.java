package com.connectedgames.stats.adapters.out.persistence;

import java.time.Instant;
import java.time.ZoneOffset;

import java.sql.Types;
import java.time.OffsetDateTime;

import com.connectedgames.stats.application.port.out.MatchResultRepository;
import com.connectedgames.stats.domain.result.MatchResult;
import com.connectedgames.stats.domain.result.ScoredPoint;
import com.connectedgames.stats.domain.result.SideResult;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class MatchReultPersistenceAdapter implements MatchResultRepository {
    private final JdbcClient jdbc;
    MatchReultPersistenceAdapter(JdbcClient jdbc)
    {
        this.jdbc=jdbc;
    }
    @Override
    @Transactional
    public boolean saveIfAbsent(MatchResult r){
        // on conflict: two deliveries of the same match at the same time cannot both write
        int inserted=jdbc.sql("""
                              INSERT INTO match_result (match_id,message_id,tenant_id,venue_id,table_id,game_type_id,
                                tournament_match_id,mode,points_to_win,reason,winner_side,started_at,ended_at)
            VALUES (:matchId,:messageId,:tenantId,:venueId,:tableId,:gameTypeId,:tournamentMatchId,:mode,:pointsToWin,:reason,:winnerSide,:startedAt,:endedAt)
            ON CONFLICT (match_id) DO NOTHING
            """)
            .param("matchId", r.id().value())
                .param("messageId", r.messageId().value())
                .param("tenantId", r.tenantId().value())
                .param("venueId", r.venueId().value())
                .param("tableId", r.tableId().value())
                .param("gameTypeId", r.gameTypeId().value())
                .param("tournamentMatchId", r.tournamentMatchId() == null ? null : r.tournamentMatchId().value(), Types.OTHER)
                .param("mode", r.mode().name())
                .param("pointsToWin", r.pointsToWin())
                .param("reason", r.reason().name())
                .param("winnerSide", r.winnerSide() == null ? null : r.winnerSide().name(), Types.VARCHAR)
                .param("startedAt", at(r.startedAt()))
                .param("endedAt", at(r.endedAt()))
                .update();
        if(inserted==0)
        {
            return false;
        }
        for(SideResult side : r.sides()){
            jdbc.sql("""
                            INSERT INTO match_side_result (match_id, side, participant_id, display_name, score)
                            VALUES (:matchId, :side, :participantId, :displayName, :score)
                            """)
                    .param("matchId", r.id().value())
                    .param("side", side.side().name())
                    .param("participantId", side.participantId(), Types.VARCHAR)
                    .param("displayName", side.displayName(), Types.VARCHAR)
                    .param("score", side.score())
                    .update();
        }
        for(ScoredPoint point : r.points()){
            jdbc.sql("""
                            INSERT INTO scored_point (match_id, seq, side, occurred_at)
                            VALUES (:matchId, :seq, :side, :occurredAt)
                            """)
                    .param("matchId", r.id().value())
                    .param("seq", point.seq())
                    .param("side", point.side().name())
                    .param("occurredAt", at(point.occurredAt()))
                    .update();
        }
        return true;
    }
    private static OffsetDateTime at(Instant instant){
        return instant.atOffset(ZoneOffset.UTC);
    }
}
