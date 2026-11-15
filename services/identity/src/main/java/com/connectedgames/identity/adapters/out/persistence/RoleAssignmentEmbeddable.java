package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

/** One row of "user_role", owned by its user. */
@Embeddable
class RoleAssignmentEmbeddable {

    @Enumerated(EnumType.STRING)
    private Role role;
    private UUID venueId;
    private UUID organizationId;

    protected RoleAssignmentEmbeddable() {
        // required by JPA
    }

    static RoleAssignmentEmbeddable from(RoleAssignment assignment) {
        RoleAssignmentEmbeddable row = new RoleAssignmentEmbeddable();
        row.role = assignment.role();
        row.venueId = assignment.venueId();
        row.organizationId = assignment.organizationId();
        return row;
    }

    RoleAssignment toDomain() {
        return new RoleAssignment(role, venueId, organizationId);
    }
}
