package com.connectedgames.tournament.domain;

import java.util.Objects;

public record ParticipantId(String value) {

    public ParticipantId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("participantId cannot be blank");
        }
    }
}
