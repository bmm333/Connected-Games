package com.connectedgames.tournament.domain.tournament;

import java.util.Objects;

import com.connectedgames.tournament.domain.format.SingleElimination;
import com.connectedgames.tournament.domain.format.TournamentFormat;

public record SingleEliminationSpec(Seeding seeding, boolean thirdPlaceMatch) implements FormatSpec {

    public SingleEliminationSpec {
        Objects.requireNonNull(seeding, "seeding");
    }

    @Override
    public TournamentFormat format() {
        return new SingleElimination(thirdPlaceMatch);
    }
}
