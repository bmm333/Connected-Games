package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.GuestSession;
import com.connectedgames.identity.application.port.in.UserSession;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T20:00:00Z");
    private static final Email EMAIL = new Email("giulia@example.com");

    private final TestClock clock = new TestClock(NOW);
    private final InMemoryGuestRepository guests = new InMemoryGuestRepository();
    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final InMemoryRefreshTokenRepository refreshTokens = new InMemoryRefreshTokenRepository();
    private final FakeRefreshTokenGenerator tokenGenerator = new FakeRefreshTokenGenerator();
    private final FakePasswordHasher passwordHasher = new FakePasswordHasher();
    private final SessionTokens tokens = new SessionTokens(refreshTokens, tokenGenerator, new FakeAccessTokenIssuer());

    private final CreateGuestSessionService guestSessions = new CreateGuestSessionService(guests, sessions, tokens, clock);
    private final LoginService login = new LoginService(users, passwordHasher, sessions, tokens, clock);
    private final RefreshSessionService refresh = new RefreshSessionService(refreshTokens, tokenGenerator,
            sessions, guests, users, tokens, clock);
    private final RegisterService service = new RegisterService(users, passwordHasher, sessions, tokens, clock);

    private UserSession registerGiulia(Optional<GuestSession> asGuest) {
        return service.register(EMAIL, "secret123", "Giulia Ferri", asGuest.map(guest -> guest.session().id()));
    }

    @Test
    void createsAPlayerAndLogsItIn() {
        UserSession registered = registerGiulia(Optional.empty());

        assertThat(users.findByEmail(EMAIL)).contains(registered.user());
        assertThat(registered.user().displayName()).isEqualTo("Giulia Ferri");
        assertThat(registered.user().roles()).containsExactly(RoleAssignment.of(Role.PLAYER));
        assertThat(registered.session().kind()).isEqualTo(SessionKind.USER);
        assertThat(registered.session().refreshExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
    }

    @Test
    void storesOnlyTheHashOfThePassword() {
        UserSession registered = registerGiulia(Optional.empty());

        assertThat(registered.user().passwordHash().value()).isEqualTo("hashed:secret123");
    }

    @Test
    void theNewUserCanLogIn() {
        registerGiulia(Optional.empty());

        assertThat(login.login(EMAIL, "secret123").user().email()).isEqualTo(EMAIL);
    }

    @Test
    void rejectsAnEmailAlreadyRegisteredWhateverItsCase() {
        registerGiulia(Optional.empty());

        assertThatThrownBy(() -> service.register(new Email("GIULIA@example.com"), "other-pass", "Giulia Bis",
                Optional.empty())).isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void aGuestKeepsItsPlayer() {
        GuestSession guest = guestSessions.createGuestSession(new Nickname("Giulia"));

        UserSession registered = registerGiulia(Optional.of(guest));

        assertThat(registered.user().playerId()).isEqualTo(guest.guest().playerId());
    }

    @Test
    void theGuestSessionEnds() {
        GuestSession guest = guestSessions.createGuestSession(new Nickname("Giulia"));
        clock.advance(Duration.ofMinutes(10));

        registerGiulia(Optional.of(guest));

        Session guestSession = sessions.findById(guest.session().id()).orElseThrow();
        assertThat(guestSession.revokedAt()).isEqualTo(clock.instant());
        assertThatThrownBy(() -> refresh.refreshSession(guest.session().refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void aUserSessionCannotBeTakenOver() {
        UserSession other = service.register(new Email("mario@example.com"), "secret123", "Mario Rossi",
                Optional.empty());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.register(EMAIL, "secret123", "Giulia Ferri", Optional.of(other.session().id())));
        assertThat(users.findByEmail(EMAIL)).isEmpty();
    }

    @Test
    void anExpiredGuestSessionCannotBeTakenOver() {
        GuestSession guest = guestSessions.createGuestSession(new Nickname("Giulia"));
        clock.advance(Duration.ofHours(12));

        assertThatIllegalArgumentException().isThrownBy(() -> registerGiulia(Optional.of(guest)));
        assertThat(users.findByEmail(EMAIL)).isEmpty();
    }
}
