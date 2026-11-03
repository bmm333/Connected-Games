package com.connectedgames.identity.domain.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SessionTest {

    private static final Instant NOW = Instant.parse("2026-11-03T20:00:00Z");

    private Session start(SessionKind kind) {
        return Session.start(new SessionId(UUID.randomUUID()), kind, UUID.randomUUID(), NOW);
    }

    @ParameterizedTest
    @EnumSource(SessionKind.class)
    void startsActiveWithTheRefreshLifetimeOfItsKind(SessionKind kind) {
        Session session = start(kind);

        assertThat(session.startedAt()).isEqualTo(NOW);
        assertThat(session.refreshExpiresAt()).isEqualTo(NOW.plus(kind.refreshLifetime()));
        assertThat(session.revokedAt()).isNull();
        assertThat(session.isActive(NOW)).isTrue();
    }

    @Test
    void guestSessionLastsUntilTheNextMorning() {
        assertThat(start(SessionKind.GUEST).refreshExpiresAt()).isEqualTo(Instant.parse("2026-11-04T08:00:00Z"));
    }

    @Test
    void isActiveUntilJustBeforeItsExpiry() {
        Session session = start(SessionKind.GUEST);

        assertThat(session.isActive(session.refreshExpiresAt().minusSeconds(1))).isTrue();
        assertThat(session.isActive(session.refreshExpiresAt())).isFalse();
        assertThat(session.isActive(session.refreshExpiresAt().plusSeconds(1))).isFalse();
    }

    @Test
    void refreshingAUserSessionMovesItsExpiry() {
        Session session = start(SessionKind.USER);
        Instant later = NOW.plus(Duration.ofDays(10));

        Session refreshed = session.refreshed(later);

        assertThat(refreshed.refreshExpiresAt()).isEqualTo(later.plus(Duration.ofDays(30)));
        assertThat(refreshed.startedAt()).isEqualTo(NOW);
        assertThat(refreshed.id()).isEqualTo(session.id());
    }

    @Test
    void refreshingAGuestSessionKeepsItsExpiry() {
        Session session = start(SessionKind.GUEST);

        Session refreshed = session.refreshed(NOW.plus(Duration.ofHours(11)));

        assertThat(refreshed.refreshExpiresAt()).isEqualTo(session.refreshExpiresAt());
    }

    @Test
    void refreshingDoesNotChangeTheOriginalSession() {
        Session session = start(SessionKind.USER);
        Instant expiry = session.refreshExpiresAt();

        session.refreshed(NOW.plus(Duration.ofDays(10)));

        assertThat(session.refreshExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void cannotRefreshAnExpiredSession() {
        Session session = start(SessionKind.GUEST);

        assertThatIllegalStateException().isThrownBy(() -> session.refreshed(session.refreshExpiresAt()));
    }

    @Test
    void cannotRefreshARevokedSession() {
        Session revoked = start(SessionKind.USER).revoked(NOW.plusSeconds(60));

        assertThatIllegalStateException().isThrownBy(() -> revoked.refreshed(NOW.plusSeconds(120)));
    }

    @Test
    void revokingEndsTheSession() {
        Instant logout = NOW.plus(Duration.ofHours(1));

        Session revoked = start(SessionKind.USER).revoked(logout);

        assertThat(revoked.revokedAt()).isEqualTo(logout);
        assertThat(revoked.isActive(logout)).isFalse();
    }

    @Test
    void revokingTwiceKeepsTheFirstRevocation() {
        Instant first = NOW.plus(Duration.ofHours(1));

        Session revoked = start(SessionKind.USER).revoked(first).revoked(first.plusSeconds(60));

        assertThat(revoked.revokedAt()).isEqualTo(first);
    }

    @Test
    void cannotExpireBeforeItStarts() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Session(new SessionId(UUID.randomUUID()),
                SessionKind.USER, UUID.randomUUID(), NOW, NOW, null));
    }

    @Test
    void rejectsNullFields() {
        assertThatNullPointerException().isThrownBy(() -> Session.start(new SessionId(UUID.randomUUID()),
                null, UUID.randomUUID(), NOW));
    }
}
