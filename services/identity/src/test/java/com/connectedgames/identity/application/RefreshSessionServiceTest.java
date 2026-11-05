package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.ActiveSession;
import com.connectedgames.identity.application.port.in.GuestSession;
import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.identity.application.IdentityFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T20:00:00Z");
    private static final RoleAssignment PLAYER = RoleAssignment.of(Role.PLAYER);

    private final TestClock clock = new TestClock(NOW);
    private final InMemoryGuestRepository guests = new InMemoryGuestRepository();
    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final InMemoryRefreshTokenRepository refreshTokens = new InMemoryRefreshTokenRepository();
    private final FakeRefreshTokenGenerator tokenGenerator = new FakeRefreshTokenGenerator();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final SessionTokens tokens = new SessionTokens(refreshTokens, tokenGenerator, accessTokens);

    private final CreateGuestSessionService guestSessions = new CreateGuestSessionService(guests, sessions, tokens, clock);
    private final LoginService login = new LoginService(users, new FakePasswordHasher(), sessions, tokens, clock);
    private final RefreshSessionService service = new RefreshSessionService(refreshTokens, tokenGenerator,
            sessions, guests, users, tokens, clock);

    private final User mario = user("mario@example.com", "secret123", List.of(PLAYER));

    @BeforeEach
    void setUp() {
        users.save(mario);
    }

    private String loginAsMario() {
        return login.login(new Email("mario@example.com"), "secret123").session().refreshToken();
    }

    private Session savedSession(ActiveSession active) {
        return sessions.findById(active.session().id()).orElseThrow();
    }

    @Test
    void givesANewRefreshTokenAndUsesUpTheOldOne() {
        String first = loginAsMario();
        clock.advance(Duration.ofHours(1));

        ActiveSession refreshed = service.refreshSession(first);

        assertThat(refreshed.session().refreshToken()).isEqualTo("refresh-2");
        assertThat(refreshTokens.findByHash("hash:refresh-1").orElseThrow().usedAt()).isEqualTo(clock.instant());
        assertThat(refreshTokens.findByHash("hash:refresh-2").orElseThrow().isUsed()).isFalse();
    }

    @Test
    void keepsTheSameSession() {
        UserSession logged = login.login(new Email("mario@example.com"), "secret123");

        ActiveSession refreshed = service.refreshSession(logged.session().refreshToken());

        assertThat(refreshed.session().id()).isEqualTo(logged.session().id());
    }

    @Test
    void extendsAUserSessionAndReturnsTheUser() {
        String first = loginAsMario();
        clock.advance(Duration.ofDays(10));

        ActiveSession refreshed = service.refreshSession(first);

        assertThat(refreshed).isInstanceOf(UserSession.class);
        assertThat(((UserSession) refreshed).user()).isEqualTo(mario);
        assertThat(refreshed.session().refreshExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(30)));
        assertThat(savedSession(refreshed).refreshExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(30)));
    }

    @Test
    void keepsTheExpiryOfAGuestSessionAndReturnsTheGuest() {
        GuestSession created = guestSessions.createGuestSession(new Nickname("Bomber"));
        clock.advance(Duration.ofHours(11));

        ActiveSession refreshed = service.refreshSession(created.session().refreshToken());

        assertThat(refreshed).isEqualTo(new GuestSession(created.guest(), refreshed.session()));
        assertThat(refreshed.session().refreshExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(12)));
        assertThat(accessTokens.lastRoles()).containsExactly(PLAYER);
    }

    @Test
    void readsTheRolesOfTheUserAgain() {
        String first = loginAsMario();
        RoleAssignment manager = RoleAssignment.venueManager(UUID.randomUUID(), UUID.randomUUID());
        users.save(new User(mario.id(), mario.email(), mario.displayName(), mario.passwordHash(),
                mario.playerId(), List.of(PLAYER, manager)));

        service.refreshSession(first);

        assertThat(accessTokens.lastRoles()).containsExactly(PLAYER, manager);
    }

    @Test
    void rejectsAnUnknownToken() {
        assertThatThrownBy(() -> service.refreshSession("never-issued"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void reusingATokenRevokesTheWholeSession() {
        String first = loginAsMario();
        String second = service.refreshSession(first).session().refreshToken();
        clock.advance(Duration.ofMinutes(5));

        assertThatThrownBy(() -> service.refreshSession(first)).isInstanceOf(InvalidRefreshTokenException.class);

        Session revoked = sessions.findById(refreshTokens.findByHash("hash:" + first).orElseThrow().sessionId()).orElseThrow();
        assertThat(revoked.revokedAt()).isEqualTo(clock.instant());
        assertThatThrownBy(() -> service.refreshSession(second)).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsTheTokenOfAnExpiredSession() {
        String token = guestSessions.createGuestSession(new Nickname("Bomber")).session().refreshToken();
        clock.advance(Duration.ofHours(12));

        assertThatThrownBy(() -> service.refreshSession(token)).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsTheTokenOfARevokedSession() {
        UserSession logged = login.login(new Email("mario@example.com"), "secret123");
        Session session = sessions.findById(logged.session().id()).orElseThrow();
        sessions.save(session.revoked(NOW));

        assertThatThrownBy(() -> service.refreshSession(logged.session().refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class);
        assertThat(refreshTokens.findByHash("hash:refresh-1").orElseThrow().isUsed()).isFalse();
    }
}
