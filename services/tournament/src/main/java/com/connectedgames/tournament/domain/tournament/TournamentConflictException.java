package com.connectedgames.tournament.domain.tournament;

public class TournamentConflictException extends RuntimeException {

    public TournamentConflictException(String message) {
        super(message);
    }
}
