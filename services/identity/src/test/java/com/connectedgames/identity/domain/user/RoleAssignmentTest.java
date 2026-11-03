package com.connectedgames.identity.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class RoleAssignmentTest {

    private static final UUID VENUE = UUID.fromString("b0000000-0000-0000-0000-000000000001");
    private static final UUID ORGANIZATION = UUID.fromString("a0000000-0000-0000-0000-000000000001");

    @Test
    void venueManagerIsTiedToAVenueAndAnOrganization() {
        RoleAssignment assignment = RoleAssignment.venueManager(VENUE, ORGANIZATION);

        assertThat(assignment.role()).isEqualTo(Role.VENUE_MANAGER);
        assertThat(assignment.venueId()).isEqualTo(VENUE);
        assertThat(assignment.organizationId()).isEqualTo(ORGANIZATION);
    }

    @Test
    void venueManagerNeedsAVenue() {
        assertThatIllegalArgumentException().isThrownBy(() -> RoleAssignment.venueManager(null, ORGANIZATION));
    }

    @Test
    void venueManagerNeedsAnOrganization() {
        assertThatIllegalArgumentException().isThrownBy(() -> RoleAssignment.venueManager(VENUE, null));
    }

    @Test
    void venueManagerCannotBeCreatedWithoutIds() {
        assertThatIllegalArgumentException().isThrownBy(() -> RoleAssignment.of(Role.VENUE_MANAGER));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = "VENUE_MANAGER", mode = EnumSource.Mode.EXCLUDE)
    void otherRolesAreGlobal(Role role) {
        RoleAssignment assignment = RoleAssignment.of(role);

        assertThat(assignment.venueId()).isNull();
        assertThat(assignment.organizationId()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = "VENUE_MANAGER", mode = EnumSource.Mode.EXCLUDE)
    void otherRolesRejectAVenue(Role role) {
        assertThatIllegalArgumentException().isThrownBy(() -> new RoleAssignment(role, VENUE, null));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = "VENUE_MANAGER", mode = EnumSource.Mode.EXCLUDE)
    void otherRolesRejectAnOrganization(Role role) {
        assertThatIllegalArgumentException().isThrownBy(() -> new RoleAssignment(role, null, ORGANIZATION));
    }

    @Test
    void rejectsNullRole() {
        assertThatNullPointerException().isThrownBy(() -> RoleAssignment.of(null));
    }
}
