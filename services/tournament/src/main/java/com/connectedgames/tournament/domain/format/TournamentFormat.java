package com.connectedgames.tournament.domain.format;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import com.connectedgames.tournament.domain.TournamentMatchId;
import com.connectedgames.tournament.domain.match.Entrant;
import com.connectedgames.tournament.domain.match.TournamentMatch;

public interface TournamentFormat {

    List<TournamentMatch> createMatches(List<Entrant> inSeedOrder, Instant now, Supplier<TournamentMatchId> ids);
}
