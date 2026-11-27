package com.connectedgames.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
import com.connectedgames.stats.domain.result.SideResult;

/** Closed matches with just the details a test cares about: the winner follows from the scores. */
public final class MatchBuilder {

    private TenantId tenant = new TenantId("acme");
    private VenueId venue = new VenueId(UUID.randomUUID());
    private GameTypeId game = Fixtures.GAME_TYPE;
    private TournamentMatchId tournament;
    private String redId;
    private String redName;
    private String blueId;
    private String blueName;
    private int redScore = 5;
    private int blueScore = 3;
    private Instant endedAt = Fixtures.END;
    private Duration duration = Duration.ofMinutes(10);
    private String points = "";

    public static MatchBuilder match() {
        return new MatchBuilder();
    }

    public MatchBuilder tenant(String value) {
        this.tenant = new TenantId(value);
        return this;
    }

    public MatchBuilder venue(VenueId value) {
        this.venue = value;
        return this;
    }

    public MatchBuilder game(GameTypeId value) {
        this.game = value;
        return this;
    }

    public MatchBuilder tournament(TournamentMatchId value) {
        this.tournament = value;
        return this;
    }

    public MatchBuilder red(String participantId, String displayName) {
        this.redId = participantId;
        this.redName = displayName;
        return this;
    }

    public MatchBuilder blue(String participantId, String displayName) {
        this.blueId = participantId;
        this.blueName = displayName;
        return this;
    }

    public MatchBuilder score(int red, int blue) {
        this.redScore = red;
        this.blueScore = blue;
        return this;
    }

    public MatchBuilder endedAt(Instant value) {
        this.endedAt = value;
        return this;
    }

    public MatchBuilder duration(Duration value) {
        this.duration = value;
        return this;
    }

    public MatchBuilder points(String sequence) {
        this.points = sequence;
        return this;
    }

    public MatchResult build() {
        Side winner = redScore > blueScore ? Side.RED : blueScore > redScore ? Side.BLUE : null;
        return new MatchResult(new MatchId(UUID.randomUUID()), new MessageId(UUID.randomUUID()), tenant, venue,
                new TableId(UUID.randomUUID()), game, tournament, GameMode.TEAM, 5,
                winner == null ? EndReason.TIME_EXPIRED : EndReason.SCORE_REACHED, winner,
                endedAt.minus(duration), endedAt,
                List.of(new SideResult(Side.RED, redId, redName, redScore),
                        new SideResult(Side.BLUE, blueId, blueName, blueScore)),
                Fixtures.points(points));
    }
}
