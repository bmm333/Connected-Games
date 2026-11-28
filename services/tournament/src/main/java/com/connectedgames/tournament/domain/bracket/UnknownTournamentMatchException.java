package com.connectedgames.tournament.domain.bracket;

import com.connectedgames.tournament.domain.TournamentMatchId;

public class UnknownTournamentMatchException extends RuntimeException{
    public UnknownTournamentMatchException(TournamentMatchId id){
        super("Tournament match " + id.value() + "not found");
    }
}
