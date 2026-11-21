package com.connectedgames.match.domain;

import java.util.Objects;
import java.util.UUID;

/** Match of a tournament bracket, id of tournament: no foreign key across services. */
public record TournamentMatchId(UUID value) {
    public TournamentMatchId { Objects.requireNonNull(value); }
}
