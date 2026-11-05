package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.identity.application.IdentityFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T20:00:00Z");

    private final TestClock clock = new TestClock(NOW);
    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final InMemoryRefreshTokenRepository refreshTokens = new InMemoryRefreshTokenRepository();
    private final FakeRefreshTokenGenerator tokenGenerator = new FakeRefreshTokenGenerator();
    private final SessionTokens tokens = new SessionTokens(refreshTokens, tokenGenerator, new FakeAccessTokenIssuer());

    private final LoginService login = new LoginService(users, new FakePasswordHasher(), sessions, tokens, clock);
    private final RefreshSessionService refresh = new RefreshSessionService(refreshTokens, tokenGenerator,
            sessions, new InMemoryGuestRepository(), users, tokens, clock);
    private final LogoutService service = new LogoutService(sessions, clock);

    @BeforeEach
    void setUp() {
        users.save(user("mario@example.com", "secret123", List.of(RoleAssignment.of(Role.PLAYER))));
    }

    private UserSession loginAsMario() {
        return login.login(new Email("mario@example.com"), "secret123");
    }

    @Test
    void revokesTheSession() {
        SessionId id = loginAsMario().session().id();
        clock.advance(Duration.ofHours(1));

        service.logout(id);

        assertThat(sessions.findById(id).orElseThrow().revokedAt()).isEqualTo(clock.instant());
    }

    @Test
    void theRefreshTokenStopsWorking() {
        UserSession logged = loginAsMario();

        service.logout(logged.session().id());

        assertThatThrownBy(() -> refresh.refreshSession(logged.session().refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void otherSessionsOfTheSameUserStayOpen() {
        UserSession phone = loginAsMario();
        UserSession laptop = loginAsMario();

        service.logout(phone.session().id());

        assertThat(sessions.findById(laptop.session().id()).orElseThrow().isActive(NOW)).isTrue();
        assertThat(refresh.refreshSession(laptop.session().refreshToken())).isNotNull();
    }

    @Test
    void loggingOutTwiceKeepsTheFirstTime() {
        SessionId id = loginAsMario().session().id();
        service.logout(id);
        clock.advance(Duration.ofMinutes(5));

        service.logout(id);

        assertThat(sessions.findById(id).orElseThrow().revokedAt()).isEqualTo(NOW);
    }

    @Test
    void aMissingSessionIsABug() {
        assertThatThrownBy(() -> service.logout(new SessionId(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);
    }
}
