package com.connectedgames.match.domain;

import java.util.Objects;
import java.util.UUID;

public record MatchId(UUID value) {
    public MatchId { Objects.requireNonNull(value); }
}
