package com.connectedgames.tournament.domain.tournament;

import java.time.Instant;
import java.util.Objects;

import com.connectedgames.tournament.domain.VenueId;

public record TournamentDetails(String name, String description, VenueId venueId, Instant startsAt,
                                Instant registrationClosesAt, int minParticipants, Integer maxParticipants) {

    public static final int MAX_PARTICIPANTS = 128;

    public TournamentDetails {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("The name has from 1 to 120 characters");
        }
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("The description has at most 2000 characters");
        }
        if (minParticipants < 2) {
            throw new IllegalArgumentException("A tournament needs at least 2 participants");
        }
        if (maxParticipants != null && maxParticipants < minParticipants) {
            throw new IllegalArgumentException("The maximum cannot be lower than the minimum");
        }
        if (maxParticipants != null && maxParticipants > MAX_PARTICIPANTS) {
            throw new IllegalArgumentException("A tournament has at most " + MAX_PARTICIPANTS + " participants");
        }
    }

    public int capacity() {
        return maxParticipants == null ? MAX_PARTICIPANTS : maxParticipants;
    }
}
