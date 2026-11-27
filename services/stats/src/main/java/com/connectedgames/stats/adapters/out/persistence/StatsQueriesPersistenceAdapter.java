package com.connectedgames.stats.adapters.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import com.connectedgames.stats.application.Viewer;
import com.connectedgames.stats.application.port.out.StatsQueries;
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
import com.connectedgames.stats.domain.result.Outcome;
import com.connectedgames.stats.domain.result.ScoredPoint;
import com.connectedgames.stats.domain.result.SideResult;
import com.connectedgames.stats.domain.stats.GameTypeTotals;
import com.connectedgames.stats.domain.stats.LeaderboardEntry;
import com.connectedgames.stats.domain.stats.Page;
import com.connectedgames.stats.domain.stats.ParticipantMatch;
import com.connectedgames.stats.domain.stats.ParticipantSummary;
import com.connectedgames.stats.domain.stats.StatsFilter;
import com.connectedgames.stats.domain.stats.Totals;
import com.connectedgames.stats.domain.stats.VenueGameTypeTotals;
import com.connectedgames.stats.domain.stats.VenueSummary;

@Component
class StatsQueriesPersistenceAdapter implements StatsQueries{

    private static final String PARTICIPANT_SIDES = """
            FROM match_side_result s
            JOIN match_result m ON m.match_id = s.match_id
            JOIN match_side_result o ON o.match_id = s.match_id AND o.side <> s.side
            """;

    private static final String TOTALS = """
            COUNT(*) AS matches,
            COUNT(*) FILTER (WHERE m.winner_side = s.side) AS wins,
            COUNT(*) FILTER (WHERE m.winner_side IS NOT NULL AND m.winner_side <> s.side) AS losses,
            COUNT(*) FILTER (WHERE m.winner_side IS NULL) AS draws,
            SUM(s.score) AS points_for,
            SUM(o.score) AS points_against
            """;

    private final JdbcClient jdbc;

    StatsQueriesPersistenceAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<LeaderboardEntry> leaderboard(Set<TenantId> tenants, StatsFilter filter, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenants", tenantValues(tenants));
        params.put("limit", limit);
        String sql = """
                SELECT RANK() OVER (ORDER BY wins DESC, points_for - points_against DESC) AS ranking,
                       participant_id, display_name, matches, wins, losses, draws, points_for, points_against
                FROM (
                    SELECT s.participant_id,
                           (ARRAY_AGG(s.display_name ORDER BY m.ended_at DESC)
                                FILTER (WHERE s.display_name IS NOT NULL))[1] AS display_name,
                """ + TOTALS + PARTICIPANT_SIDES + """
                    WHERE s.participant_id IS NOT NULL AND m.tenant_id IN (:tenants)
                """ + filterSql(filter, params) + """
                    GROUP BY s.participant_id
                ) totals
                ORDER BY ranking, participant_id
                LIMIT :limit
                """;
        return jdbc.sql(sql).params(params).query((rs, row) -> new LeaderboardEntry(rs.getInt("ranking"),
                rs.getString("participant_id"), rs.getString("display_name"), totals(rs))).list();
    }

     @Override
    public boolean participantKnown(Viewer viewer, String participantId) {
        Map<String, Object> params = new HashMap<>();
        params.put("participantId", participantId);
        String sql = "SELECT EXISTS (SELECT 1 FROM match_side_result s JOIN match_result m ON m.match_id = s.match_id "
                + "WHERE s.participant_id = :participantId" + viewerSql(viewer, params) + ")";
        return Boolean.TRUE.equals(jdbc.sql(sql).params(params).query(Boolean.class).single());
    }
    
    @Override
    public ParticipantSummary participantSummary(Viewer viewer, String participantId, StatsFilter filter) {
        Map<String, Object> params = new HashMap<>();
        params.put("participantId", participantId);
        String viewerSql = viewerSql(viewer, params);
        String sql = "SELECT m.game_type_id, MAX(m.ended_at) AS last_match_at, " + TOTALS + PARTICIPANT_SIDES
                + " WHERE s.participant_id = :participantId" + viewerSql + filterSql(filter, params)
                + " GROUP BY m.game_type_id ORDER BY matches DESC, m.game_type_id";
        List<GameTypeTotals> byGameType = new ArrayList<>();
        List<Instant> lastMatches = new ArrayList<>();
        jdbc.sql(sql).params(params).query(rs -> {
            byGameType.add(new GameTypeTotals(new GameTypeId(rs.getObject("game_type_id", UUID.class)), totals(rs)));
            lastMatches.add(instant(rs, "last_match_at"));
        });

        int matches = 0;
        int wins = 0;
        int losses = 0;
        int draws = 0;
        int pointsFor = 0;
        int pointsAgainst = 0;
        for (GameTypeTotals game : byGameType) {
            matches += game.totals().matches();
            wins += game.totals().wins();
            losses += game.totals().losses();
            draws += game.totals().draws();
            pointsFor += game.totals().pointsFor();
            pointsAgainst += game.totals().pointsAgainst();
        }
        Instant lastMatchAt = lastMatches.stream().max(Instant::compareTo).orElse(null);

        Map<String, Object> nameParams = new HashMap<>();
        nameParams.put("participantId", participantId);
        String nameSql = "SELECT s.display_name FROM match_side_result s JOIN match_result m ON m.match_id = s.match_id "
                + "WHERE s.participant_id = :participantId AND s.display_name IS NOT NULL"
                + viewerSql(viewer, nameParams) + " ORDER BY m.ended_at DESC LIMIT 1";
        String displayName = jdbc.sql(nameSql).params(nameParams).query(String.class).optional().orElse(null);

        return new ParticipantSummary(participantId, displayName, lastMatchAt,
                new Totals(matches, wins, losses, draws, pointsFor, pointsAgainst), byGameType);
    }

    @Override
    public List<ParticipantMatch> participantMatches(Viewer viewer, String participantId, StatsFilter filter,
                                                     Page page) {
        Map<String, Object> params = new HashMap<>();
        params.put("participantId", participantId);
        params.put("limit", page.limit());
        params.put("offset", page.offset());
        String sql = """
                SELECT m.match_id, m.venue_id, m.table_id, m.game_type_id, m.tournament_match_id, m.ended_at,
                       EXTRACT(EPOCH FROM (m.ended_at - m.started_at))::int AS duration_seconds,
                       s.side,
                       CASE WHEN m.winner_side IS NULL THEN 'DRAW'
                            WHEN m.winner_side = s.side THEN 'WIN' ELSE 'LOSS' END AS result,
                       s.score AS points_for, o.score AS points_against,
                       o.participant_id AS opponent_participant_id, o.display_name AS opponent_display_name
                """ + PARTICIPANT_SIDES + " WHERE s.participant_id = :participantId" + viewerSql(viewer, params)
                + filterSql(filter, params) + " ORDER BY m.ended_at DESC, m.match_id LIMIT :limit OFFSET :offset";
        return jdbc.sql(sql).params(params).query((rs, row) -> {
            UUID tournament = rs.getObject("tournament_match_id", UUID.class);
            return new ParticipantMatch(new MatchId(rs.getObject("match_id", UUID.class)),
                    new VenueId(rs.getObject("venue_id", UUID.class)),
                    new TableId(rs.getObject("table_id", UUID.class)),
                    new GameTypeId(rs.getObject("game_type_id", UUID.class)),
                    tournament == null ? null : new TournamentMatchId(tournament),
                    instant(rs, "ended_at"), rs.getInt("duration_seconds"), Side.valueOf(rs.getString("side")),
                    Outcome.valueOf(rs.getString("result")), rs.getInt("points_for"), rs.getInt("points_against"),
                    rs.getString("opponent_participant_id"), rs.getString("opponent_display_name"));
        }).list();
    }
    @Override
    public Optional<MatchResult> matchResult(Viewer viewer, MatchId matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId.value());
        String visible = switch (viewer) {
            case Viewer.OfTenants t -> {
                params.put("viewerTenants", tenantValues(t.tenants()));
                yield "m.tenant_id IN (:viewerTenants)";
            }
            case Viewer.OfPlayer p -> {
                params.put("viewerPlayer", p.playerId());
                yield "EXISTS (SELECT 1 FROM match_side_result v WHERE v.match_id = m.match_id "
                        + "AND v.participant_id = :viewerPlayer)";
            }
        };
        return jdbc.sql("SELECT m.* FROM match_result m WHERE m.match_id = :matchId AND " + visible)
                .params(params).query((rs, row) -> toResult(rs)).optional();
    }

    private MatchResult toResult(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("match_id", UUID.class);
        UUID tournament = rs.getObject("tournament_match_id", UUID.class);
        String winner = rs.getString("winner_side");
        List<SideResult> sides = jdbc.sql("""
                        SELECT side, participant_id, display_name, score FROM match_side_result
                        WHERE match_id = :id ORDER BY CASE side WHEN 'RED' THEN 0 ELSE 1 END
                        """).param("id", id)
                .query((s, row) -> new SideResult(Side.valueOf(s.getString("side")), s.getString("participant_id"),
                        s.getString("display_name"), s.getInt("score"))).list();
        List<ScoredPoint> points = jdbc.sql("SELECT seq, side, occurred_at FROM scored_point WHERE match_id = :id ORDER BY seq")
                .param("id", id)
                .query((p, row) -> new ScoredPoint(p.getInt("seq"), Side.valueOf(p.getString("side")),
                        instant(p, "occurred_at"))).list();
        return new MatchResult(new MatchId(id), new MessageId(rs.getObject("message_id", UUID.class)),
                new TenantId(rs.getString("tenant_id")), new VenueId(rs.getObject("venue_id", UUID.class)),
                new TableId(rs.getObject("table_id", UUID.class)),
                new GameTypeId(rs.getObject("game_type_id", UUID.class)),
                tournament == null ? null : new TournamentMatchId(tournament),
                GameMode.valueOf(rs.getString("mode")), rs.getInt("points_to_win"),
                EndReason.valueOf(rs.getString("reason")), winner == null ? null : Side.valueOf(winner),
                instant(rs, "started_at"), instant(rs, "ended_at"), sides, points);
    }
    @Override
    public boolean venueKnown(Set<TenantId> tenants, VenueId venueId) {
        return Boolean.TRUE.equals(jdbc.sql("SELECT EXISTS (SELECT 1 FROM match_result m "
                        + "WHERE m.tenant_id IN (:tenants) AND m.venue_id = :venueId)")
                .param("tenants", tenantValues(tenants)).param("venueId", venueId.value())
                .query(Boolean.class).single());
    }

    @Override
    public VenueSummary venueSummary(Set<TenantId> tenants, VenueId venueId, StatsFilter filter) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenants", tenantValues(tenants));
        params.put("venueId", venueId.value());
        StatsFilter withoutVenue = new StatsFilter(null, filter.gameTypeId(), filter.from(), filter.to());
        String sql = """
                SELECT m.game_type_id,
                       COUNT(*) AS matches,
                       SUM((SELECT SUM(s.score) FROM match_side_result s WHERE s.match_id = m.match_id)) AS total_points,
                       SUM(EXTRACT(EPOCH FROM (m.ended_at - m.started_at))) AS duration_seconds,
                       MAX(m.ended_at) AS last_match_at
                FROM match_result m
                WHERE m.tenant_id IN (:tenants) AND m.venue_id = :venueId
                """ + filterSql(withoutVenue, params) + """
                GROUP BY m.game_type_id
                ORDER BY matches DESC, m.game_type_id
                """;
        List<VenueGameTypeTotals> byGameType = new ArrayList<>();
        List<Instant> lastMatches = new ArrayList<>();
        int matches = 0;
        int totalPoints = 0;
        double durationSeconds = 0;
        for (GameRow game : jdbc.sql(sql).params(params).query((rs, row) -> new GameRow(
                new GameTypeId(rs.getObject("game_type_id", UUID.class)), rs.getInt("matches"),
                rs.getInt("total_points"), rs.getDouble("duration_seconds"), instant(rs, "last_match_at"))).list()) {
            byGameType.add(new VenueGameTypeTotals(game.gameTypeId(), game.matches(), game.totalPoints(),
                    average(game.durationSeconds(), game.matches())));
            lastMatches.add(game.lastMatchAt());
            matches += game.matches();
            totalPoints += game.totalPoints();
            durationSeconds += game.durationSeconds();
        }
        return new VenueSummary(venueId, matches, totalPoints, average(durationSeconds, matches),
                lastMatches.stream().max(Instant::compareTo).orElse(null), byGameType);
    }

    private record GameRow(GameTypeId gameTypeId, int matches, int totalPoints, double durationSeconds,
                           Instant lastMatchAt) {
    }

    private static int average(double seconds, int matches) {
        return matches == 0 ? 0 : (int) Math.round(seconds / matches);
    }

    private static Totals totals(ResultSet rs) throws SQLException {
        return new Totals(rs.getInt("matches"), rs.getInt("wins"), rs.getInt("losses"), rs.getInt("draws"),
                rs.getInt("points_for"), rs.getInt("points_against"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static List<String> tenantValues(Set<TenantId> tenants) {
        return tenants.stream().map(TenantId::value).toList();
    }

    private static String viewerSql(Viewer viewer, Map<String, Object> params) {
        return switch (viewer) {
            case Viewer.OfTenants t -> {
                params.put("viewerTenants", tenantValues(t.tenants()));
                yield " AND m.tenant_id IN (:viewerTenants)";
            }
            case Viewer.OfPlayer p -> {
                params.put("viewerPlayer", p.playerId());
                yield " AND s.participant_id = :viewerPlayer";
            }
        };
    }

    private static String filterSql(StatsFilter filter, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        if (filter.venueId() != null) {
            sql.append(" AND m.venue_id = :filterVenue");
            params.put("filterVenue", filter.venueId().value());
        }
        if (filter.gameTypeId() != null) {
            sql.append(" AND m.game_type_id = :filterGameType");
            params.put("filterGameType", filter.gameTypeId().value());
        }
        if (filter.from() != null) {
            sql.append(" AND m.ended_at >= :filterFrom");
            params.put("filterFrom", filter.from().atOffset(ZoneOffset.UTC));
        }
        if (filter.to() != null) {
            sql.append(" AND m.ended_at < :filterTo");
            params.put("filterTo", filter.to().atOffset(ZoneOffset.UTC));
        }
        return sql.append(' ').toString();
    }

}
