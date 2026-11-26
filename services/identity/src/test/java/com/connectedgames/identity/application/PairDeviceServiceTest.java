package com.connectedgames.identity.application;

import com.connectedgames.identity.application.port.in.ActiveSession;
import com.connectedgames.identity.application.port.in.DeviceSession;
import com.connectedgames.identity.domain.device.Device;
import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import com.connectedgames.identity.domain.session.Session;
import com.connectedgames.identity.domain.session.SessionKind;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PairDeviceServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-18T20:00:00Z");
    private static final UUID VENUE = UUID.randomUUID();
    private static final UUID ORGANIZATION = UUID.randomUUID();
    private static final UUID TABLE = UUID.randomUUID();

    private final TestClock clock = new TestClock(NOW);
    private final InMemoryPairingCodeRepository codes = new InMemoryPairingCodeRepository();
    private final InMemoryDeviceRepository devices = new InMemoryDeviceRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final InMemoryRefreshTokenRepository refreshTokens = new InMemoryRefreshTokenRepository();
    private final FakeRefreshTokenGenerator tokenGenerator = new FakeRefreshTokenGenerator();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final SessionTokens tokens = new SessionTokens(refreshTokens, tokenGenerator, accessTokens);

    private final PairDeviceService service = new PairDeviceService(codes, devices, sessions, tokens, clock);
    private final RefreshSessionService refresh = new RefreshSessionService(refreshTokens, tokenGenerator, sessions,
            new InMemoryGuestRepository(), new InMemoryUserRepository(), devices, tokens, clock);

    private void issue(String value, UUID table) {
        codes.save(PairingCode.issue(value, VENUE, ORGANIZATION, table, Surface.KIOSK, clock.instant()));
    }

    @Test
    void theCodeGivesTheDisplayItsDeviceAndItsSession() {
        issue("K7F2QX", TABLE);

        DeviceSession paired = service.pairDevice("K7F2QX", Surface.KIOSK);

        Device device = paired.device();
        assertThat(device.venueId()).isEqualTo(VENUE);
        assertThat(device.organizationId()).isEqualTo(ORGANIZATION);
        assertThat(device.tableId()).isEqualTo(TABLE);
        assertThat(device.surface()).isEqualTo(Surface.KIOSK);
        assertThat(device.pairedAt()).isEqualTo(NOW);
        assertThat(devices.findById(device.id())).contains(device);

        assertThat(paired.session().kind()).isEqualTo(SessionKind.DEVICE);
        assertThat(paired.session().refreshExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(90)));
        Session session = sessions.findById(paired.session().id()).orElseThrow();
        assertThat(session.subjectId()).isEqualTo(device.id().value());
    }

    @Test
    void aDisplayHasNoRoles() {
        issue("K7F2QX", TABLE);

        service.pairDevice("K7F2QX", Surface.KIOSK);

        assertThat(accessTokens.lastRoles()).isEmpty();
        assertThat(accessTokens.lastSession().kind()).isEqualTo(SessionKind.DEVICE);
    }

    @Test
    void theTokenOfTheDisplayIsIssuedForItsDevice() {
        issue("K7F2QX", TABLE);

        DeviceSession paired = service.pairDevice("K7F2QX", Surface.KIOSK);

        assertThat(accessTokens.lastDevice()).isEqualTo(paired.device());
        assertThat(accessTokens.lastDevice().organizationId()).isEqualTo(ORGANIZATION);
    }

    @Test
    void theCodeWorksOnlyOnce() {
        issue("K7F2QX", TABLE);
        service.pairDevice("K7F2QX", Surface.KIOSK);

        assertThatThrownBy(() -> service.pairDevice("K7F2QX", Surface.KIOSK))
                .isInstanceOf(InvalidPairingCodeException.class);
        assertThat(devices.count()).isEqualTo(1);
    }

    @Test
    void anExpiredCodeIsRefused() {
        issue("K7F2QX", TABLE);
        clock.advance(Duration.ofMinutes(10));

        assertThatThrownBy(() -> service.pairDevice("K7F2QX", Surface.KIOSK))
                .isInstanceOf(InvalidPairingCodeException.class);
        assertThat(devices.count()).isZero();
    }

    @Test
    void anUnknownCodeIsRefusedLikeAnExpiredOne() {
        assertThatThrownBy(() -> service.pairDevice("NOPE00", Surface.KIOSK))
                .isInstanceOf(InvalidPairingCodeException.class)
                .hasMessage(new InvalidPairingCodeException().getMessage());
    }

    @Test
    void aNewDisplayReplacesThePreviousOneOfTheTable() {
        issue("AAAAAA", TABLE);
        DeviceSession old = service.pairDevice("AAAAAA", Surface.KIOSK);
        issue("BBBBBB", TABLE);

        DeviceSession replacement = service.pairDevice("BBBBBB", Surface.KIOSK);

        assertThat(devices.count()).isEqualTo(1);
        assertThat(devices.findById(old.device().id())).isEmpty();
        assertThat(devices.findByTable(TABLE, Surface.KIOSK)).contains(replacement.device());
        // the old display cannot renew its session any more
        assertThat(sessions.findById(old.session().id()).orElseThrow().revokedAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> refresh.refreshSession(old.session().refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class);
        // and the new one can
        assertThat(refresh.refreshSession(replacement.session().refreshToken())).isInstanceOf(DeviceSession.class);
    }

    @Test
    void anotherTableKeepsItsDisplay() {
        issue("AAAAAA", TABLE);
        DeviceSession first = service.pairDevice("AAAAAA", Surface.KIOSK);
        issue("BBBBBB", UUID.randomUUID());

        service.pairDevice("BBBBBB", Surface.KIOSK);

        assertThat(devices.count()).isEqualTo(2);
        assertThat(sessions.findById(first.session().id()).orElseThrow().revokedAt()).isNull();
    }

    @Test
    void theDisplayRefreshesItsSessionAndStaysAttachedToItsTable() {
        issue("K7F2QX", TABLE);
        DeviceSession paired = service.pairDevice("K7F2QX", Surface.KIOSK);
        clock.advance(Duration.ofDays(1));

        ActiveSession refreshed = refresh.refreshSession(paired.session().refreshToken());

        DeviceSession device = (DeviceSession) refreshed;
        assertThat(device.device()).isEqualTo(paired.device());
        assertThat(device.session().refreshToken()).isNotEqualTo(paired.session().refreshToken());
        // a device session is extended each time, like a user one
        assertThat(device.session().refreshExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(90)));
    }

    @Test
    void theRefreshedTokenCarriesTheOrganizationAgain() {
        issue("K7F2QX", TABLE);
        DeviceSession paired = service.pairDevice("K7F2QX", Surface.KIOSK);

        refresh.refreshSession(paired.session().refreshToken());

        assertThat(accessTokens.lastDevice()).isEqualTo(paired.device());
        assertThat(accessTokens.lastRoles()).isEmpty();
    }
}
