package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.GuestSession;
import com.connectedgames.identity.application.port.in.IssuedSession;
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
            new SessionTokens(refreshTokens, new FakeRefreshTokenGenerator(), accessTokens),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private Session savedSession(IssuedSession issued) {
        return sessions.findById(issued.id()).orElseThrow();
    }

    @Test
    void createsAndSavesAGuestWithTheChosenNickname() {
        GuestSession created = service.createGuestSession(new Nickname("Bomber"));

        assertThat(created.guest().nickname()).isEqualTo(new Nickname("Bomber"));
        assertThat(guests.findByPlayerId(created.guest().playerId())).contains(created.guest());
    }

    @Test
    void theSessionBelongsToTheGuestPlayer() {
        GuestSession created = service.createGuestSession(new Nickname("Bomber"));

        assertThat(savedSession(created.session()).subjectId()).isEqualTo(created.guest().playerId().value());
    }

    @Test
    void startsAGuestSessionThatEndsTwelveHoursLater() {
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber")).session();

        assertThat(issued.kind()).isEqualTo(SessionKind.GUEST);
        assertThat(issued.refreshExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(12)));
        assertThat(savedSession(issued).startedAt()).isEqualTo(NOW);
        assertThat(savedSession(issued).isActive(NOW)).isTrue();
    }

    @Test
    void storesOnlyTheHashOfTheRefreshToken() {
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber")).session();

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
        IssuedSession issued = service.createGuestSession(new Nickname("Bomber")).session();

        assertThat(accessTokens.lastSession()).isEqualTo(savedSession(issued));
        assertThat(accessTokens.lastRoles()).containsExactly(RoleAssignment.of(Role.PLAYER));
        assertThat(issued.accessToken()).isEqualTo("access-" + issued.id().value());
        assertThat(issued.accessTokenExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
    }

    @Test
    void everyGuestGetsItsOwnPlayerAndSession() {
        GuestSession first = service.createGuestSession(new Nickname("Bomber"));
        GuestSession second = service.createGuestSession(new Nickname("Bomber"));

        assertThat(second.guest().playerId()).isNotEqualTo(first.guest().playerId());
        assertThat(second.session().id()).isNotEqualTo(first.session().id());
        assertThat(second.session().refreshToken()).isNotEqualTo(first.session().refreshToken());
    }
}
