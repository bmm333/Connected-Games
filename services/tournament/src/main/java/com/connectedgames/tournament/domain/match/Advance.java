package com.connectedgames.tournament.domain.match;

import java.util.Objects;

import com.connectedgames.tournament.domain.Side;
import com.connectedgames.tournament.domain.TournamentMatchId;

public record Advance(TournamentMatchId matchId, Side side) {

    public Advance {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(side, "side");
    }
}
