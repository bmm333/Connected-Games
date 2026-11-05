package com.connectedgames.identity.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 *  A role of a user, Venue_manager is tied to one venue of one org (tenant)
 *  the other roles are global. Venues belong to catalog identity keeps only their ids
 * */
public record RoleAssignment(Role role,UUID venueId,UUID organizationId)
    {
    public RoleAssignment{
        Objects.requireNonNull(role,"role");
        if(role==Role.VENUE_MANAGER&&(venueId==null||organizationId==null))
            throw new IllegalArgumentException("A venue manager needs a venue and an organization");
        if(role!=Role.VENUE_MANAGER&&(venueId!=null||organizationId!=null))
            throw new IllegalArgumentException("Role " + role + " is not tied to a venue");
    }
    public static RoleAssignment of(Role role)
    {
        return new RoleAssignment(role,null,null);
    }
    public static RoleAssignment venueManager(UUID venueId,UUID organizationId)
{
        return new RoleAssignment(Role.VENUE_MANAGER, venueId, organizationId);
    }
}
