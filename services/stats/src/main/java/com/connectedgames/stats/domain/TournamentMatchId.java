package com.connectedgames.stats.domain;

import java.util.Objects;
import java.util.UUID;

public record TournamentMatchId(UUID value) {

    public TournamentMatchId {
        Objects.requireNonNull(value, "value");
    }
}
