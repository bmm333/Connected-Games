package com.connectedgames.tournament.domain.registration;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.connectedgames.tournament.domain.ParticipantId;
import com.connectedgames.tournament.domain.PlayerId;
import com.connectedgames.tournament.domain.RegistrationId;

public record Registration(RegistrationId id, ParticipantId participantId, String displayName, List<Member> members,
                           Integer seed, Instant registeredAt) {

    public Registration {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(participantId, "participantId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(members, "members");
        Objects.requireNonNull(registeredAt, "registeredAt");
        if (displayName.isBlank() || displayName.length() > 60) {
            throw new IllegalArgumentException("The name of a registration has from 1 to 60 characters");
        }
        members = List.copyOf(members);
        if (members.isEmpty()) {
            throw new IllegalArgumentException("A registration has at least one member");
        }
        if (new HashSet<>(members.stream().map(Member::playerId).toList()).size() != members.size()) {
            throw new IllegalArgumentException("A player is twice in the registration");
        }
        if (seed != null && seed < 1) {
            throw new IllegalArgumentException("seed starts from 1");
        }
    }

    public Registration withSeed(Integer newSeed) {
        return new Registration(id, participantId, displayName, members, newSeed, registeredAt);
    }

    public boolean hasPlayer(PlayerId playerId) {
        return members.stream().anyMatch(member -> member.playerId().equals(playerId));
    }
}
