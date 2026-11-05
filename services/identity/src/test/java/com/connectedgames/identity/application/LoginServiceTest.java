package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.identity.application.IdentityFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T20:00:00Z");
    private static final RoleAssignment PLAYER = RoleAssignment.of(Role.PLAYER);

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final InMemoryRefreshTokenRepository refreshTokens = new InMemoryRefreshTokenRepository();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final LoginService service = new LoginService(users, new FakePasswordHasher(), sessions,
            new SessionTokens(refreshTokens, new FakeRefreshTokenGenerator(), accessTokens),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final User manager = user("mario@example.com", "secret123",
            List.of(PLAYER, RoleAssignment.venueManager(UUID.randomUUID(), UUID.randomUUID())));

    @BeforeEach
    void setUp() {
        users.save(manager);
    }

    @Test
    void startsAUserSessionThatLastsThirtyDays() {
        UserSession logged = service.login(new Email("mario@example.com"), "secret123");

        assertThat(logged.user()).isEqualTo(manager);
        assertThat(logged.session().kind()).isEqualTo(SessionKind.USER);
        assertThat(logged.session().refreshExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
    }

    @Test
    void theSessionBelongsToTheAccountNotToThePlayer() {
        UserSession logged = service.login(new Email("mario@example.com"), "secret123");

        Session saved = sessions.findById(logged.session().id()).orElseThrow();
        assertThat(saved.subjectId()).isEqualTo(manager.id().value());
    }

    @Test
    void signsTheAccessTokenWithTheRolesOfTheUser() {
        service.login(new Email("mario@example.com"), "secret123");

        assertThat(accessTokens.lastRoles()).isEqualTo(manager.roles());
    }

    @Test
    void ignoresTheCaseOfTheEmail() {
        UserSession logged = service.login(new Email("MARIO@Example.com"), "secret123");

        assertThat(logged.user()).isEqualTo(manager);
    }

    @Test
    void rejectsAWrongPassword() {
        assertThatThrownBy(() -> service.login(new Email("mario@example.com"), "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsAnUnknownEmailWithTheSameError() {
        assertThatThrownBy(() -> service.login(new Email("nobody@example.com"), "secret123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Wrong email or password");
    }

    @Test
    void aFailedLoginCreatesNothing() {
        assertThatThrownBy(() -> service.login(new Email("mario@example.com"), "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(sessions.count()).isZero();
        assertThat(refreshTokens.count()).isZero();
        assertThat(accessTokens.lastSession()).isNull();
    }
}
