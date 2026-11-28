package com.connectedgames.tournament.domain.match;

import java.util.Objects;

import com.connectedgames.tournament.domain.ParticipantId;

public record Entrant(ParticipantId id, String displayName) {

    public Entrant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName cannot be blank");
        }
    }
}
