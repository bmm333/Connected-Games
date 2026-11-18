package com.connectedgames.match.application;

public class MatchNotFoundException extends RuntimeException {

    public MatchNotFoundException() {
        super("Match not found");
    }
}
