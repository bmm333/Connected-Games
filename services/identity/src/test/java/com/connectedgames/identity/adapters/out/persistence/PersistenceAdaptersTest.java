package com.connectedgames.identity.adapters.out.persistence;

import com.connectedgames.identity.TestcontainersConfiguration;
import com.connectedgames.identity.application.InvalidRefreshTokenException;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.session.RefreshToken;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionId;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Email;
import com.connectedgames.identity.domain.user.PasswordHash;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import com.connectedgames.identity.domain.user.UserId;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Saves and loads through the adapters on a real PostgreSQL migrated by Flyway. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, UserPersistenceAdapter.class, GuestPersistenceAdapter.class,
        SessionPersistenceAdapter.class, RefreshTokenPersistenceAdapter.class})
class PersistenceAdaptersTest {

    private static final Instant NOW = Instant.parse("2026-11-05T20:00:00Z");
    private static final RoleAssignment PLAYER = RoleAssignment.of(Role.PLAYER);

    @Autowired UserPersistenceAdapter users;
    @Autowired GuestPersistenceAdapter guests;
    @Autowired SessionPersistenceAdapter sessions;
    @Autowired RefreshTokenPersistenceAdapter refreshTokens;
    @Autowired TestEntityManager em;

    /** Writes to the database and empties the JPA cache, so the next read really hits the tables */
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private User user(String email, List<RoleAssignment> roles) {
        return new User(new UserId(UUID.randomUUID()), new Email(email), "Mario Rossi",
                new PasswordHash("$2b$10$hash"), new PlayerId(UUID.randomUUID()), roles);
    }

    private Session savedSession() {
        Session session = Session.start(new SessionId(UUID.randomUUID()), SessionKind.USER, UUID.randomUUID(), NOW);
        sessions.save(session);
        return session;
    }

    @Test
    void savesAUserWithItsRolesInOrder() {
        RoleAssignment manager = RoleAssignment.venueManager(UUID.randomUUID(), UUID.randomUUID());
        User mario = user("mario@example.com", List.of(PLAYER, manager));

        users.save(mario);
        flushAndClear();

        assertThat(users.findById(mario.id())).contains(mario);
        assertThat(users.findByEmail(new Email("mario@example.com"))).contains(mario);
    }

    @Test
    void anUnknownEmailIsEmpty() {
        assertThat(users.findByEmail(new Email("nobody@example.com"))).isEmpty();
    }

    @Test
    void savingAUserAgainReplacesItsRoles() {
        RoleAssignment manager = RoleAssignment.venueManager(UUID.randomUUID(), UUID.randomUUID());
        User mario = user("mario@example.com", List.of(PLAYER, manager));
        users.save(mario);
        flushAndClear();

        users.save(new User(mario.id(), mario.email(), mario.displayName(), mario.passwordHash(),
                mario.playerId(), List.of(PLAYER)));
        flushAndClear();

        assertThat(users.findById(mario.id()).orElseThrow().roles()).containsExactly(PLAYER);
    }

    @Test
    void theDatabaseRefusesASecondAccountWithTheSameEmail() {
        users.save(user("mario@example.com", List.of(PLAYER)));
        flushAndClear();

        users.save(user("mario@example.com", List.of(PLAYER)));

        assertThatThrownBy(this::flushAndClear).isInstanceOf(PersistenceException.class);
    }

    @Test
    void savesAGuest() {
        Guest guest = new Guest(new PlayerId(UUID.randomUUID()), new Nickname("Bomber"));

        guests.save(guest);
        flushAndClear();

        assertThat(guests.findByPlayerId(guest.playerId())).contains(guest);
    }

    @Test
    void savesASessionAndItsRevocation() {
        Session session = savedSession();
        flushAndClear();
        assertThat(sessions.findById(session.id())).contains(session);

        Session revoked = session.revoked(NOW.plus(Duration.ofHours(1)));
        sessions.save(revoked);
        flushAndClear();

        assertThat(sessions.findById(session.id())).contains(revoked);
    }

    @Test
    void savesARefreshTokenAndMarksItUsed() {
        Session session = savedSession();
        RefreshToken token = RefreshToken.issue("hash-1", session.id(), NOW);
        refreshTokens.save(token);
        flushAndClear();
        assertThat(refreshTokens.findByHash("hash-1")).contains(token);

        refreshTokens.save(token.used(NOW.plusSeconds(60)));
        flushAndClear();

        assertThat(refreshTokens.findByHash("hash-1").orElseThrow().usedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void onlyOneOfTwoRefreshesWithTheSameTokenCanUseIt() {
        Session session = savedSession();
        RefreshToken token = RefreshToken.issue("hash-1", session.id(), NOW);
        refreshTokens.save(token);
        flushAndClear();

        // both requests loaded the token before either marked it
        refreshTokens.save(token.used(NOW.plusSeconds(60)));

        assertThatThrownBy(() -> refreshTokens.save(token.used(NOW.plusSeconds(61))))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
