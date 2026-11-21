package com.connectedgames.match;

import java.util.UUID;

import com.connectedgames.match.domain.GameTypeId;
import com.connectedgames.match.domain.TenantId;
import com.connectedgames.match.domain.TournamentMatchId;

/** Values shared by the tests of the fields that every MatchStarted carries. */
public final class Fixtures {

    public static final TenantId TENANT = new TenantId("00000000-0000-4000-8000-0000000000a1");
    public static final GameTypeId GAME_TYPE = new GameTypeId(UUID.fromString("00000000-0000-4000-8000-0000000000b1"));
    public static final TournamentMatchId TOURNAMENT_MATCH =
            new TournamentMatchId(UUID.fromString("00000000-0000-4000-8000-0000000000c1"));

    private Fixtures() {
    }
}
