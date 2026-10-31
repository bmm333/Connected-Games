package com.connectedgames.identity.domain.user;

import com.connectedgames.identity.domain.PlayerId;

import java.util.List;
import java.util.Objects;

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

    public boolean hasRole(Role role) {
        return roles.stream().anyMatch(assignment -> assignment.role() == role);
    }
}
