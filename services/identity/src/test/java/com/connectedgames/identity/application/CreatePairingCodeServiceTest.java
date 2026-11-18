package com.connectedgames.identity.application;

import com.connectedgames.identity.domain.device.PairingCode;
import com.connectedgames.identity.domain.device.Surface;
import com.connectedgames.identity.domain.user.Role;
import com.connectedgames.identity.domain.user.RoleAssignment;
import com.connectedgames.identity.domain.user.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.connectedgames.identity.application.IdentityFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreatePairingCodeServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-18T20:00:00Z");
    private static final UUID VENUE = UUID.randomUUID();
    private static final UUID TABLE = UUID.randomUUID();

    private final TestClock clock = new TestClock(NOW);
    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemoryPairingCodeRepository codes = new InMemoryPairingCodeRepository();

    private final User manager = user("gestore@example.com", "secret123",
            List.of(RoleAssignment.of(Role.PLAYER), RoleAssignment.venueManager(VENUE, UUID.randomUUID())));

    private CreatePairingCodeService service(String... generated) {
        users.save(manager);
        return new CreatePairingCodeService(users, codes, new FakePairingCodeGenerator(generated), clock);
    }

    @Test
    void theManagerOfTheVenueGetsACodeValidForTenMinutes() {
        PairingCode code = service("K7F2QX").createPairingCode(manager.id(), VENUE, TABLE, Surface.KIOSK);

        assertThat(code.value()).isEqualTo("K7F2QX");
        assertThat(code.venueId()).isEqualTo(VENUE);
        assertThat(code.tableId()).isEqualTo(TABLE);
        assertThat(code.surface()).isEqualTo(Surface.KIOSK);
        assertThat(code.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
        assertThat(codes.findByValueForUpdate("K7F2QX")).contains(code);
    }

    @Test
    void theManagerOfAnotherVenueIsRefused() {
        CreatePairingCodeService service = service("K7F2QX");

        assertThatThrownBy(() -> service.createPairingCode(manager.id(), UUID.randomUUID(), TABLE, Surface.KIOSK))
                .isInstanceOf(NotVenueManagerException.class);
        assertThat(codes.findByValueForUpdate("K7F2QX")).isEmpty();
    }

    @Test
    void aPlayerIsRefused() {
        User player = user("giulia@example.com", "secret123", List.of(RoleAssignment.of(Role.PLAYER)));
        users.save(player);
        CreatePairingCodeService service = service("K7F2QX");

        assertThatThrownBy(() -> service.createPairingCode(player.id(), VENUE, TABLE, Surface.KIOSK))
                .isInstanceOf(NotVenueManagerException.class);
    }

    @Test
    void aCodeStillValidIsNeverGivenTwice() {
        CreatePairingCodeService service = service("AAAAAA", "AAAAAA", "BBBBBB");
        service.createPairingCode(manager.id(), VENUE, TABLE, Surface.KIOSK);

        PairingCode second = service.createPairingCode(manager.id(), VENUE, TABLE, Surface.KIOSK);

        assertThat(second.value()).isEqualTo("BBBBBB");
    }

    @Test
    void anExpiredCodeCanBeGivenAgain() {
        CreatePairingCodeService service = service("AAAAAA", "AAAAAA");
        service.createPairingCode(manager.id(), VENUE, TABLE, Surface.KIOSK);
        clock.advance(Duration.ofMinutes(11));

        PairingCode again = service.createPairingCode(manager.id(), VENUE, TABLE, Surface.KIOSK);

        assertThat(again.value()).isEqualTo("AAAAAA");
        assertThat(again.expiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(10)));
    }
}
