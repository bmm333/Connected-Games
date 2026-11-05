package com.connectedgames.identity.domain.user;

import com.connectedgames.identity.domain.PlayerId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final RoleAssignment PLAYER = RoleAssignment.of(Role.PLAYER);

    private User userWith(String displayName, List<RoleAssignment> roles) {
        return new User(new UserId(UUID.randomUUID()), new Email("mario@example.com"), displayName,
                new PasswordHash("$2a$10$hash"), new PlayerId(UUID.randomUUID()), roles);
    }

    @Test
    void stripsTheDisplayName() {
        assertThat(userWith("  Mario  ", List.of(PLAYER)).displayName()).isEqualTo("Mario");
    }

    @Test
    void acceptsTheShortestDisplayName() {
        assertThat(userWith("Mo", List.of(PLAYER)).displayName()).isEqualTo("Mo");
    }

    @Test
    void acceptsTheLongestDisplayName() {
        String value = "a".repeat(40);

        assertThat(userWith(value, List.of(PLAYER)).displayName()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "M", "  M  "})
    void rejectsDisplayNamesShorterThan2Characters(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> userWith(value, List.of(PLAYER)));
    }

    @Test
    void rejectsDisplayNamesLongerThan40Characters() {
        assertThatIllegalArgumentException().isThrownBy(() -> userWith("a".repeat(41), List.of(PLAYER)));
    }

    @Test
    void needsAtLeastOneRole() {
        assertThatIllegalArgumentException().isThrownBy(() -> userWith("Mario", List.of()));
    }

    @Test
    void rejectsANullRole() {
        assertThatNullPointerException().isThrownBy(() -> userWith("Mario", Arrays.asList(PLAYER, null)));
    }

    @Test
    void rejectsNullFields() {
        assertThatNullPointerException().isThrownBy(() -> new User(new UserId(UUID.randomUUID()), null, "Mario",
                new PasswordHash("$2a$10$hash"), new PlayerId(UUID.randomUUID()), List.of(PLAYER)));
    }

    @Test
    void rolesCannotBeChangedFromOutside() {
        List<RoleAssignment> roles = new ArrayList<>(List.of(PLAYER));
        User user = userWith("Mario", roles);

        roles.add(RoleAssignment.of(Role.PLATFORM_ADMIN));

        assertThat(user.roles()).containsExactly(PLAYER);
        assertThatThrownBy(() -> user.roles().add(PLAYER)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void knowsItsRoles() {
        UUID venue = UUID.randomUUID();
        User manager = userWith("Gestore", List.of(PLAYER, RoleAssignment.venueManager(venue, UUID.randomUUID())));

        assertThat(manager.hasRole(Role.VENUE_MANAGER)).isTrue();
        assertThat(manager.hasRole(Role.PLAYER)).isTrue();
        assertThat(manager.hasRole(Role.PLATFORM_ADMIN)).isFalse();
    }
}
