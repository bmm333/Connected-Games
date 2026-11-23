package com.connectedgames.match;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connectedgames.match.application.port.out.MatchCompleted;
import com.connectedgames.match.domain.EventId;
import com.connectedgames.match.domain.GameMode;
import com.connectedgames.match.domain.MatchId;
import com.connectedgames.match.domain.Side;
import com.connectedgames.match.domain.TableId;
import com.connectedgames.match.domain.VenueId;
import com.connectedgames.match.domain.event.EndReason;
import com.connectedgames.match.domain.match.MatchSide;

import com.connectedgames.match.domain.GameTypeId;
import com.connectedgames.match.domain.TenantId;
import com.connectedgames.match.domain.TournamentMatchId;

/** Values shared by the tests of the fields that every MatchStarted carries. */
public final class Fixtures {

    public static final TenantId TENANT = new TenantId("00000000-0000-4000-8000-0000000000a1");
    public static final GameTypeId GAME_TYPE = new GameTypeId(UUID.fromString("00000000-0000-4000-8000-0000000000b1"));
    public static final TournamentMatchId TOURNAMENT_MATCH =
            new TournamentMatchId(UUID.fromString("00000000-0000-4000-8000-0000000000c1"));

    /** A closed match of a tournament, with new ids each time: the first one to arrive is the one the outbox keeps. */
    public static MatchCompleted completed(MatchId match) {
        return new MatchCompleted(new EventId(UUID.randomUUID()), TENANT, new VenueId(UUID.randomUUID()),
                new TableId(UUID.randomUUID()), match, GAME_TYPE, TOURNAMENT_MATCH, GameMode.TEAM, 5,
                Instant.parse("2026-11-16T20:00:00Z"), Instant.parse("2026-11-16T20:10:00Z"),
                EndReason.SCORE_REACHED, Side.RED,
                List.of(new MatchSide(Side.RED, "team-1", "Lightning", 5), new MatchSide(Side.BLUE, null, "Blue", 3)));
    }

    public static MatchCompleted completed() {
        return completed(new MatchId(UUID.randomUUID()));
    }

    private Fixtures() {
    }
}
