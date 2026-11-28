package com.connectedgames.tournament.domain.registration;

import java.util.Objects;

import com.connectedgames.tournament.domain.PlayerId;

public record Member(PlayerId playerId, String displayName) {

    public Member {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(displayName, "displayName");
        if (displayName.isBlank() || displayName.length() > 60) {
            throw new IllegalArgumentException("The name of a member has from 1 to 60 characters");
        }
    }
}
