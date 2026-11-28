package com.connectedgames.tournament.domain.tournament;

import com.connectedgames.tournament.domain.format.TournamentFormat;

public sealed interface FormatSpec permits SingleEliminationSpec {

    Seeding seeding();

    TournamentFormat format();
}
