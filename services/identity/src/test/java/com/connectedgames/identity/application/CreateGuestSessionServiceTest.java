package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.IssuedSession;
import com.connectedgames.identity.domain.PlayerId;
import com.connectedgames.identity.domain.guest.Guest;
import com.connectedgames.identity.domain.guest.Nickname;
import com.connectedgames.identity.domain.session.RefreshToken;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionKind;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CreateGuestSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T20:00:00Z");

    private final InMemoryGuestRepository guests = new InMemoryGuestRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final InMemoryRefreshTokenRepository refreshTokens = new InMemoryRefreshTokenRepository();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final CreateGuestSessionService service = new CreateGuestSessionService(guests, sessions,
            refreshTokens, new FakeRefreshTokenGenerator(), accessTokens, Clock.fixed(NOW, ZoneOffset.UTC));

    private Session savedSession(IssuedSession issued) {
        return sessions.findById(issued.id()).orElseThrow();
    }

    @Test
    void createsAGuestWithTheChosenNickname() {
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber"));

        PlayerId player = new PlayerId(savedSession(issued).subjectId());
        Guest guest = guests.findByPlayerId(player).orElseThrow();

        assertThat(guest.nickname()).isEqualTo(new Nickname("Bomber"));
    }

    @Test
    void startsAGuestSessionThatEndsTwelveHoursLater() {
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber"));

        assertThat(issued.kind()).isEqualTo(SessionKind.GUEST);
        assertThat(issued.refreshExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(12)));
        assertThat(savedSession(issued).startedAt()).isEqualTo(NOW);
        assertThat(savedSession(issued).isActive(NOW)).isTrue();
    }

    @Test
    void storesOnlyTheHashOfTheRefreshToken() {
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber"));

        assertThat(issued.refreshToken()).isEqualTo("refresh-1");
        assertThat(refreshTokens.findByHash("refresh-1")).isEmpty();

        RefreshToken stored = refreshTokens.findByHash("hash:refresh-1").orElseThrow();
        assertThat(stored.sessionId()).isEqualTo(issued.id());
        assertThat(stored.issuedAt()).isEqualTo(NOW);
        assertThat(stored.isUsed()).isFalse();
        assertThat(refreshTokens.count()).isEqualTo(1);
    }

    @Test
    void signsTheAccessTokenForTheNewSessionAsAPlayer() {
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber"));

        assertThat(accessTokens.lastSession()).isEqualTo(savedSession(issued));
        assertThat(accessTokens.lastRoles()).containsExactly(RoleAssignment.of(Role.PLAYER));
        assertThat(issued.accessToken()).isEqualTo("access-" + issued.id().value());
        assertThat(issued.accessTokenExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
    }

    @Test
    void everyGuestGetsItsOwnPlayerAndSession() {
        IssuedSession first = service.createGuestSession(new Nickname("Bomber"));
        IssuedSession second = service.createGuestSession(new Nickname("Bomber"));

        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(savedSession(second).subjectId()).isNotEqualTo(savedSession(first).subjectId());
        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
    }
}
