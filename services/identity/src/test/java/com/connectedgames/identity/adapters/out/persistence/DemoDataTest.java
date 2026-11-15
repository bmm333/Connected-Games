package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.TestcontainersConfiguration;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The demo users of the local profile load and map to the domain. */
@DataJpaTest(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/demo")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, UserPersistenceAdapter.class})
class DemoDataTest {

    private static final UUID BAR_BELVEDERE = UUID.fromString("b0000000-0000-4000-8000-000000000001");
    private static final UUID GRUPPO_BELVEDERE = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    @Autowired UserPersistenceAdapter users;

    @Test
    void theManagerOfBarBelvedereIsAlsoAPlayer() {
        User manager = users.findByEmail(new Email("gestore@belvedere.example")).orElseThrow();

        assertThat(manager.roles()).containsExactly(RoleAssignment.of(Role.PLAYER),
                RoleAssignment.venueManager(BAR_BELVEDERE, GRUPPO_BELVEDERE));
    }

    @Test
    void thereAreAPlayerAndAnAdmin() {
        assertThat(users.findByEmail(new Email("giulia@example.com")).orElseThrow().hasRole(Role.PLAYER)).isTrue();
        assertThat(users.findByEmail(new Email("admin@connectedgames.example")).orElseThrow()
                .hasRole(Role.PLATFORM_ADMIN)).isTrue();
    }
}
