package com.connectedgames.stats;

import java.util.UUID;

/** MatchCompleted messages as JSON, built by hand as another service would write them. */
public final class Messages {

    /** RED wins 5-3 a tournament match, with three points. */
    public static String completed(UUID matchId) {
        return """
                {"eventId":"%s","tenantId":"acme","venueId":"%s","tableId":"%s","matchId":"%s",
                 "gameTypeId":"00000000-0000-4000-8000-0000000000b1",
                 "tournamentMatchId":"00000000-0000-4000-8000-0000000000c1",
                 "mode":"TEAM","pointsToWin":5,"startedAt":"2026-11-16T20:00:00Z","endedAt":"2026-11-16T20:10:00Z",
                 "reason":"SCORE_REACHED","winnerSide":"RED",
                 "sides":[{"side":"RED","participantId":"team-1","displayName":"Lightning","score":5},
                          {"side":"BLUE","displayName":"Blue","score":3}],
                 "points":[{"seq":2,"side":"RED","occurredAt":"2026-11-16T20:00:30Z"},
                           {"seq":3,"side":"BLUE","occurredAt":"2026-11-16T20:01:00Z"},
                           {"seq":4,"side":"RED","occurredAt":"2026-11-16T20:01:30Z"}]}"""
                .formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), matchId);
    }

    /** Only what the schema requires: a draw of a friendly match, nobody logged in, no points. */
    public static String draw(UUID matchId) {
        return """
                {"eventId":"%s","tenantId":"acme","venueId":"%s","tableId":"%s","matchId":"%s",
                 "gameTypeId":"00000000-0000-4000-8000-0000000000b1","mode":"INDIVIDUAL","pointsToWin":5,
                 "startedAt":"2026-11-16T20:00:00Z","endedAt":"2026-11-16T20:10:00Z","reason":"TIME_EXPIRED",
                 "sides":[{"side":"RED","score":2},{"side":"BLUE","score":2}],"points":[]}"""
                .formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), matchId);
    }

    public static String cancelled(UUID matchId) {
        return draw(matchId).replace("TIME_EXPIRED", "CANCELLED");
    }

    private Messages() {
    }
}
