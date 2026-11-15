package com.connectedgames.identity.application;

import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.connectedgames.identity.application.IdentityFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class GetCurrentUserServiceTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final GetCurrentUserService service = new GetCurrentUserService(users);

    @Test
    void returnsTheUserOfTheToken() {
        User mario = user("mario@example.com", "secret123", List.of(RoleAssignment.of(Role.PLAYER)));
        users.save(mario);

        assertThat(service.getCurrentUser(mario.id())).isEqualTo(mario);
    }

    @Test
    void aMissingUserIsABug() {
        assertThatIllegalStateException().isThrownBy(() -> service.getCurrentUser(new UserId(UUID.randomUUID())));
    }
}
