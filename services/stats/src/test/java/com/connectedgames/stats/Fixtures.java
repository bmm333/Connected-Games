package com.connectedgames.stats;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Closed matches for the tests, with new ids each time. */
public final class Fixtures {

    public static final TenantId TENANT = new TenantId("acme");
    public static final GameTypeId GAME_TYPE = new GameTypeId(UUID.fromString("00000000-0000-4000-8000-0000000000b1"));
    public static final TournamentMatchId TOURNAMENT_MATCH =
            new TournamentMatchId(UUID.fromString("00000000-0000-4000-8000-0000000000c1"));
    public static final Instant START = Instant.parse("2026-11-16T20:00:00Z");
    public static final Instant END = Instant.parse("2026-11-16T20:10:00Z");

    /** RED wins 5-3 a tournament match, with the points one by one. */
    public static MatchResult result(MatchId id) {
        return new MatchResult(id, new MessageId(UUID.randomUUID()), TENANT, new VenueId(UUID.randomUUID()),
                new TableId(UUID.randomUUID()), GAME_TYPE, TOURNAMENT_MATCH, GameMode.TEAM, 5,
                EndReason.SCORE_REACHED, Side.RED, START, END,
                List.of(new SideResult(Side.RED, "team-1", "Lightning", 5), new SideResult(Side.BLUE, null, "Blue", 3)),
                points("RRBRBRBR"));
    }

    public static MatchResult result() {
        return result(new MatchId(UUID.randomUUID()));
    }

    /** A friendly match that ended 2-2 when the time ran out: no winner, no points, nobody logged in. */
    public static MatchResult draw(MatchId id) {
        return new MatchResult(id, new MessageId(UUID.randomUUID()), TENANT, new VenueId(UUID.randomUUID()),
                new TableId(UUID.randomUUID()), GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                EndReason.TIME_EXPIRED, null, START, END,
                List.of(new SideResult(Side.RED, null, null, 2), new SideResult(Side.BLUE, null, null, 2)),
                List.of());
    }

    public static MatchResult cancelled(MatchId id) {
        return new MatchResult(id, new MessageId(UUID.randomUUID()), TENANT, new VenueId(UUID.randomUUID()),
                new TableId(UUID.randomUUID()), GAME_TYPE, null, GameMode.INDIVIDUAL, 5,
                EndReason.CANCELLED, null, START, END,
                List.of(new SideResult(Side.RED, null, null, 0), new SideResult(Side.BLUE, null, null, 0)),
                List.of());
    }

    /** R is a goal of RED, B one of BLUE. seq starts at 2, after the start; a goal every 30 seconds. */
    public static List<ScoredPoint> points(String sequence) {
        List<ScoredPoint> points = new ArrayList<>();
        for (int i = 0; i < sequence.length(); i++) {
            points.add(new ScoredPoint(i + 2, sequence.charAt(i) == 'R' ? Side.RED : Side.BLUE,
                    START.plusSeconds(30L * (i + 1))));
        }
        return points;
    }

    private Fixtures() {
    }
}
