package com.connectedgames.identity.domain.user;

import com.connectedgames.identity.domain.PlayerId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record User(UserId id, Email email, String displayName, PasswordHash passwordHash,
                   PlayerId playerId, List<RoleAssignment> roles) {

    private static final int MIN_DISPLAY_NAME = 2;
    private static final int MAX_DISPLAY_NAME = 40;

    public User{
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(email,"email");
        Objects.requireNonNull(displayName,"displayName");
        Objects.requireNonNull(passwordHash,"passwordHash");
        Objects.requireNonNull(playerId,"playerId");
        Objects.requireNonNull(roles,"roles");
        displayName = displayName.strip();
        if(displayName.length() < MIN_DISPLAY_NAME || displayName.length() > MAX_DISPLAY_NAME)
            throw new IllegalArgumentException("Display name must be 2-40 characters: " + displayName);
        roles = List.copyOf(roles);
        if(roles.isEmpty())
            throw new IllegalArgumentException("A user needs at least one role");
    }
    /**Added new method: Only the manager of that very venue the same role for another venue
     * dose not count*/
    public boolean managesVenue(UUID venueId)
    {
        return roles.stream().anyMatch(assignment ->
                assignment.role() == Role.VENUE_MANAGER && 
                assignment.venueId().equals(venueId));       
    }
    public Optional<UUID> organizationOf(UUID venueId)
    {
        return roles.stream()
                .filter(assignment -> assignment.role() == Role.VENUE_MANAGER && assignment.venueId().equals(venueId))
                .map(RoleAssignment::organizationId)
                .findFirst();
    }
    public boolean hasRole(Role role) {
        return roles.stream().anyMatch(assignment -> assignment.role() == role);
    }
}
