package com.connectedgames.tournament.domain;

public enum Side {
    RED, BLUE;

    public Side opposite() {
        return this == RED ? BLUE : RED;
    }
}
